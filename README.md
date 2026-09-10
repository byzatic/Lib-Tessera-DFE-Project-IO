# Lib-Tessera-DFE-Project-IO

Java-библиотека для загрузки, изменения, сохранения, запуска и отслеживания проектов Tessera DFE.
Публичный прикладной API находится в пакете
`io.github.byzatic.tessera.lib.configio.unified`. Все операции с проектом выполняются через
единый фасад `TesseraProjectIO`.

Библиотека поддерживает формат `v1.0.0-SingleRootStrictNestedNodeTree`, Java 17 и Maven.

## Возможности

- загрузка проекта из каталога в неизменяемый `TesseraProject`;
- сохранение каталога проекта и создание сопутствующего ZIP-архива;
- экспорт проекта непосредственно в ZIP;
- добавление shared-resource, workflow-routine и service JAR;
- сохранение DSL-файлов узлов;
- обнаружение routines и services через Java SPI;
- чтение метаданных плагинов и создание runtime-экземпляров;
- наблюдение за стабильными ревизиями ZIP с проверками безопасности при распаковке.

## Подключение

```xml
<dependency>
    <groupId>io.github.byzatic</groupId>
    <artifactId>lib-tessera-dfe-project-io</artifactId>
    <version>0.0.5</version>
</dependency>
```

Для установки текущей версии в локальный Maven-репозиторий:

```shell
mvn install -DskipTests -Dgpg.skip=true
```

## Основная точка входа

Стандартную реализацию `TesseraProjectIO` создаёт `TesseraProjectIOFactory`:

```java
import io.github.byzatic.tessera.lib.configio.unified.TesseraProjectIO;
import io.github.byzatic.tessera.lib.configio.unified.TesseraProjectIOFactory;

TesseraProjectIO projectIO = TesseraProjectIOFactory.createDefault();
```

Фасад не владеет ресурсами и не требует закрытия. Если приложению нужны собственные class loader'ы
для поиска runtime-плагинов, их можно передать при создании фасада:

```java
TesseraProjectIO projectIO = TesseraProjectIOFactory
        .createWithPreloadedClassLoaders(preloadedClassLoaders);
```

## Загрузка проекта

`loadProject` возвращает отделённую от файловой системы неизменяемую модель. Она не содержит
class loader'ов и не требует вызова `close()`.

```java
import io.github.byzatic.tessera.lib.configio.unified.model.TesseraProject;

import java.nio.file.Path;

TesseraProject project = projectIO.loadProject(Path.of("MyProject"));

System.out.println(project.getName());
System.out.println(project.getNodes().size());
System.out.println(project.getConfiguration().getServices().size());
```

## Сохранение проекта и артефактов

`saveProject` записывает каталог проекта и создаёт рядом архив `<имя-каталога>.zip`.
Дополнительные артефакты передаются единым объектом `ProjectArtifacts`:

```java
import io.github.byzatic.tessera.lib.configio.unified.model.DslSource;
import io.github.byzatic.tessera.lib.configio.unified.model.ProjectArtifacts;
import io.github.byzatic.tessera.lib.configio.unified.model.SaveProjectRequest;
import io.github.byzatic.tessera.lib.configio.unified.model.SaveProjectResult;

import java.nio.file.Path;
import java.util.List;

ProjectArtifacts artifacts = ProjectArtifacts.newBuilder()
        .sharedJars(List.of(Path.of("plugins/shared-resources.jar")))
        .routineJars(List.of(Path.of("plugins/my-routine.jar")))
        .serviceJars(List.of(Path.of("plugins/my-service.jar")))
        .dslSources(List.of(
                DslSource.newBuilder()
                        .nodeId(nodeId)
                        .baseName("worker-config")
                        .content(dslContent)
                        .build()
        ))
        .build();

SaveProjectResult result = projectIO.saveProject(
        SaveProjectRequest.newBuilder()
                .projectDirectory(Path.of("output/MyProject"))
                .project(project)
                .artifacts(artifacts)
                .build()
);

System.out.println(result.getProjectDirectory());
System.out.println(result.getArchive());
```

Артефакты копируются в следующие каталоги:

| Поле `ProjectArtifacts` | Каталог проекта |
|---|---|
| `sharedJars` | `modules/shared/` |
| `routineJars` | `modules/workflow_routines/` |
| `serviceJars` | `modules/services/` |
| `dslSources` | `data/nodes/<узел>/configuration_files/*.mcg3dsl` |

Списки артефактов по умолчанию пусты. Если дополнительные файлы не нужны, используйте короткую
форму:

```java
SaveProjectResult result = projectIO.saveProject(
        SaveProjectRequest.of(Path.of("output/MyProject"), project)
);
```

## Экспорт в ZIP

`exportProject` создаёт проект во временном каталоге и оставляет только ZIP в указанном месте.
Расширение `.zip` добавляется автоматически, если оно отсутствует. Для экспорта используются те же
`ProjectArtifacts`, поэтому shared JAR также попадают в архив под `modules/shared/`.

```java
import io.github.byzatic.tessera.lib.configio.unified.model.ExportProjectRequest;

import java.nio.file.Path;

Path archive = projectIO.exportProject(
        ExportProjectRequest.newBuilder()
                .archiveDestination(Path.of("delivery/MyProject.zip"))
                .project(project)
                .artifacts(artifacts)
                .build()
);
```

Без дополнительных артефактов:

```java
Path archive = projectIO.exportProject(
        ExportProjectRequest.of(Path.of("delivery/MyProject.zip"), project)
);
```

## Создание модели проекта

Все модели в `unified.model` являются неизменяемыми `final`-классами и создаются через builder.
Входные коллекции копируются, а наружу возвращаются неизменяемые значения.

```java
import io.github.byzatic.tessera.lib.configio.unified.model.NodeConfiguration;
import io.github.byzatic.tessera.lib.configio.unified.model.NodeId;
import io.github.byzatic.tessera.lib.configio.unified.model.Pipeline;
import io.github.byzatic.tessera.lib.configio.unified.model.PipelineStage;
import io.github.byzatic.tessera.lib.configio.unified.model.ProjectConfiguration;
import io.github.byzatic.tessera.lib.configio.unified.model.ProjectNode;
import io.github.byzatic.tessera.lib.configio.unified.model.TesseraProject;

import java.util.List;
import java.util.Map;

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

## Runtime-плагины и метаданные

Для работы с routines, services и их метаданными откройте `ProjectRuntimeSession`. Сессия владеет
class loader'ами проекта и должна быть закрыта после остановки использующего её runtime.

```java
import io.github.byzatic.tessera.lib.configio.unified.ProjectRuntimeSession;
import io.github.byzatic.tessera.lib.configio.unified.RoutineCreationRequest;
import io.github.byzatic.tessera.lib.configio.unified.ServiceCreationRequest;

import java.nio.file.Path;

try (ProjectRuntimeSession runtime = projectIO.openRuntime(Path.of("MyProject"))) {
    System.out.println(runtime.getAvailableRoutineNames());
    System.out.println(runtime.getAvailableServiceNames());
    System.out.println(runtime.getRoutineMetadata());
    System.out.println(runtime.getServiceMetadata());

    var routine = runtime.createRoutine(
            RoutineCreationRequest.newBuilder()
                    .routineName("MyRoutine")
                    .api(routineApi)
                    .health(routineHealth)
                    .build()
    );

    var service = runtime.createService(
            ServiceCreationRequest.newBuilder()
                    .serviceName("MyService")
                    .api(serviceApi)
                    .health(serviceHealth)
                    .build()
    );

    runEngine(routine, service);
}
```

Routine- и service-JAR публикуют редакторские метаданные через JDK SPI из пакетов:

```text
io.github.byzatic.tessera.lib.configio.unified.spi.routine
io.github.byzatic.tessera.lib.configio.unified.spi.service
```

Провайдер routine регистрируется в:

```text
META-INF/services/io.github.byzatic.tessera.lib.configio.unified.spi.routine.RoutineEditorDescriptorProvider
```

Провайдер service регистрируется аналогично:

```text
META-INF/services/io.github.byzatic.tessera.lib.configio.unified.spi.service.ServiceEditorDescriptorProvider
```

Descriptor-модели неизменяемы и создаются через `newBuilder()`.

## Отслеживание ревизий ZIP

`watchRevisions` следит за ZIP-файлом, ждёт стабильного состояния, проверяет архив и публикует
изолированную ревизию. Возвращаемая подписка останавливает polling при закрытии.

```java
import io.github.byzatic.tessera.lib.configio.unified.ProjectRevisionError;
import io.github.byzatic.tessera.lib.configio.unified.ProjectRevisionHandle;
import io.github.byzatic.tessera.lib.configio.unified.ProjectRevisionListener;
import io.github.byzatic.tessera.lib.configio.unified.ProjectRevisionSubscription;
import io.github.byzatic.tessera.lib.configio.unified.ProjectRevisionWatchRequest;

import java.nio.file.Path;
import java.time.Duration;

ProjectRevisionWatchRequest watchRequest = ProjectRevisionWatchRequest.builder(
                Path.of("deploy/MyProject.zip"),
                Path.of("runtime/revisions")
        )
        .pollInterval(Duration.ofSeconds(1))
        .stableObservationCount(2)
        .maximumEntryCount(100_000)
        .maximumExpandedBytes(1024L * 1024L * 1024L)
        .build();

try (ProjectRevisionSubscription subscription = projectIO.watchRevisions(
        watchRequest,
        new ProjectRevisionListener() {
            @Override
            public void onRevisionAvailable(ProjectRevisionHandle revision) {
                // Listener получает владение handle и закрывает его после использования.
                activate(revision);
            }

            @Override
            public void onRevisionRejected(ProjectRevisionError error) {
                error.getCause().printStackTrace();
            }
        }
)) {
    awaitShutdown();
}
```

`ProjectRevisionHandle` предоставляет SHA-256 идентификатор, распакованный каталог,
`TesseraProject` и `openRuntime()`. Получатель callback'а отвечает за закрытие handle; при закрытии
освобождаются runtime-ресурсы и удаляется каталог ревизии.

Значения наблюдателя по умолчанию: интервал 1 секунда, 2 стабильных наблюдения, максимум 100 000
записей и 1 GiB распакованных данных.

## Формат каталога проекта

```text
MyProject/
├── data/
│   ├── Project.json
│   ├── Global.json
│   └── nodes/
│       └── <id>-<name>/
│           ├── global.json
│           ├── pipeline.json
│           └── configuration_files/
│               └── <name>.mcg3dsl
└── modules/
    ├── shared/
    ├── workflow_routines/
    └── services/
```

Для узла с `id == "#NAMED"` каталог называется `<name>`, для остальных — `<id>-<name>`.
Поддерживается одно связное дерево с одним корнем, без циклов, повторных узлов и нескольких
родителей у одного узла.

## Публичные unified-типы

| Тип | Назначение |
|---|---|
| `TesseraProjectIO` | загрузка, сохранение, экспорт, runtime и ревизии |
| `TesseraProjectIOFactory` | создание стандартного фасада |
| `TesseraProject` | полный неизменяемый проект |
| `SaveProjectRequest`, `SaveProjectResult` | команда и результат сохранения |
| `ExportProjectRequest` | команда ZIP-экспорта |
| `ProjectArtifacts` | shared/routine/service JAR и DSL-исходники |
| `ProjectRuntimeSession` | project-scoped плагины и метаданные |
| `ProjectRevisionWatchRequest` | политика polling и безопасной распаковки |
| `ProjectRevisionSubscription` | жизненный цикл наблюдателя |
| `ProjectRevisionHandle` | изолированная подготовленная ревизия |
| `TesseraProjectException` | единая ошибка операции с указанием типа операции и пути |

Пакет `io.github.byzatic.tessera.lib.configio.unified.internal` является деталью реализации и не
предназначен для использования приложениями.

## Сборка и тесты

```shell
mvn test
```

Полная локальная сборка без подписи GPG:

```shell
mvn verify -Dgpg.skip=true
```

## Лицензия

[Apache License 2.0](LICENSE)
