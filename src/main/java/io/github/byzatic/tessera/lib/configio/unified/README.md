# Единый API проектов Tessera

Каталог `unified` — изолированный миграционный слой над текущей реализацией config-io.
Он пока не заменяет и не изменяет существующий API библиотеки.

## Основная точка входа

`TesseraProjectIO` — единый прикладной фасад. Стандартную реализацию создаёт публичный
composition root `TesseraProjectIOFactory`:

```java
TesseraProjectIO projectIO = TesseraProjectIOFactory.createDefault();

TesseraProject project = projectIO.loadProject(projectDirectory);

SaveProjectResult saved = projectIO.saveProject(
        SaveProjectRequest.of(outputDirectory, project)
);

Path archive = projectIO.exportProject(
        ExportProjectRequest.of(archiveDestination, project)
);
```

Фасад поддерживает загрузку, сохранение, ZIP-экспорт, подготовку runtime-ресурсов и наблюдение
за ревизиями проекта.

## Создание моделей через Builder

Все пользовательские модели — обычные неизменяемые `final` классы. Каждый объект создаётся через fluent builder:

```java
NodeId nodeId = NodeId.newBuilder()
        .value("root:main")
        .build();

Pipeline pipeline = Pipeline.newBuilder()
        .stages(List.of(
                PipelineStage.newBuilder()
                        .id("collect")
                        .position(1)
                        .workers(List.of())
                        .build()
        ))
        .build();

ProjectNode node = ProjectNode.newBuilder()
        .nodeId(nodeId)
        .id("root")
        .name("main")
        .description("Главный узел")
        .downstream(List.of())
        .configuration(NodeConfiguration.newBuilder().build())
        .pipeline(pipeline)
        .build();

TesseraProject project = TesseraProject.newBuilder()
        .formatVersion("v1.0.0-SingleRootStrictNestedNodeTree")
        .name("MyProject")
        .configuration(ProjectConfiguration.newBuilder().build())
        .nodes(Map.of(nodeId, node))
        .build();
```

Коллекции копируются при построении и наружу возвращаются в неизменяемом виде. Для data-классов
реализованы `equals`, `hashCode` и `toString`.

## Runtime-ресурсы

Когда нужны исполняемые workflow routines и services, используется `ProjectRuntimeSession`:

```java
try (ProjectRuntimeSession runtime = projectIO.openRuntime(projectDirectory)) {
    Set<String> routines = runtime.getAvailableRoutineNames();
    Set<String> services = runtime.getAvailableServiceNames();
    List<RoutineMetadata> routineMetadata = runtime.getRoutineMetadata();
    List<ServiceMetadata> serviceMetadata = runtime.getServiceMetadata();
}
```

`ServiceMetadata` содержит отображаемое имя, описание, версию и путь service JAR, а также
immutable-список `ServiceParameterMetadata`. Параметр объявляет тип значения, default,
варианты `SELECT` и `ServiceStorageRole`, по которому UI отличает выбор входного и выходного
хранилища от обычного параметра.

Чистая модель `TesseraProject` не содержит class loader и не требует вызова `close()`.

## SPI метаданных редактора

Routine- и service-модули публикуют метаданные редактора через JDK-only контракты:

```text
io.github.byzatic.tessera.lib.configio.unified.spi.routine
io.github.byzatic.tessera.lib.configio.unified.spi.service
```

Descriptor-модели неизменяемы и создаются через `newBuilder()`. Провайдер регистрируется
по полному имени интерфейса, например:

```text
META-INF/services/io.github.byzatic.tessera.lib.configio.unified.spi.routine.RoutineEditorDescriptorProvider
```

Пакеты `routine_spi` и `service_spi` продолжают загружаться как compatibility API для JAR,
созданных до появления unified SPI. Новые модули должны использовать только `unified.spi`.

## Владение ресурсами

- `TesseraProjectIO` не хранит состояние и не требует закрытия.
- `ProjectRuntimeSession` владеет class loader’ами модулей, сервисов, metadata и shared resources.
- `ProjectRevisionSubscription` владеет polling executor.
- Listener принимает владение каждым полученным `ProjectRevisionHandle`.
- Revision handle владеет распакованным каталогом и открытой через него runtime session.

Runtime session необходимо закрывать после остановки engine runtime. Revision handle необходимо
закрывать после runtime session. Реализация handle дополнительно соблюдает этот порядок при своём
закрытии.

## Граница текущей реализации

`unified.internal.DefaultTesseraProjectIO` — внутренний совместимый адаптер. Публичный
`TesseraProjectIOFactory` выбирает и создаёт его в composition root. Адаптер вызывает существующие
V1 factory/strategy и преобразует legacy `*DataObject` в новый агрегат и обратно.

Остальные классы из `unified.internal` не являются пользовательским API. После принятия
архитектуры стандартную реализацию можно перенести в окончательный composition-root пакет.
