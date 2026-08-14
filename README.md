# lib-tessera-dfe-config-io

Java-библиотека ввода-вывода конфигурации проектов Tessera DFE. Она читает и записывает проект формата `v1.0.0-SingleRootStrictNestedNodeTree`, упаковывает проект в ZIP, загружает JAR-модули и сервисы через Java SPI и умеет отслеживать новые ревизии проекта в ZIP-архиве.

## Возможности

- загрузка полного проекта из каталога в неизменяемую доменную модель;
- сохранение доменной модели в JSON и создание ZIP-архива;
- копирование JAR-файлов workflow-модулей и сервисов в проект;
- построение цепочки class loader'ов для общих JAR-зависимостей;
- обнаружение фабрик модулей и сервисов через `ServiceLoader`;
- polling ZIP-файла, проверка стабильности и публикация изолированных ревизий;
- защита распаковки от Zip Slip, чрезмерного числа файлов и слишком большого распакованного размера.

Требуется Java 17. Сборка выполняется Maven.

## Подключение

Текущие Maven-координаты:

```xml
<dependency>
    <groupId>io.github.byzatic</groupId>
    <artifactId>lib-tessera-dfe-config-io</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

Для установки snapshot-версии в локальный Maven-репозиторий:

```shell
mvn install -DskipTests -Dgpg.skip=true
```

## Быстрый старт: загрузка проекта

Основная точка входа — `ProjectV1LoaderFactory`. Результат загрузки владеет class loader'ами общих ресурсов, поэтому его необходимо закрывать.

```java
import io.github.byzatic.lib.configio.application.loader.ProjectLoaderInterface;
import io.github.byzatic.lib.configio.domain.model.ProjectLoadResultDataObject;
import io.github.byzatic.lib.configio.infrastructure.factory.ProjectV1LoaderFactory;

import java.nio.file.Path;

ProjectLoaderInterface loader = ProjectV1LoaderFactory.create();

try (ProjectLoadResultDataObject project = loader.load(Path.of("MyProject"))) {
    String projectName = project
            .getNodeContainer()
            .getProjectStructure()
            .getProject()
            .getProjectName();

    System.out.println(projectName);
    System.out.println(project.getGlobal().getServices().size());
}
```

Если приложению уже доступны собственные class loader'ы, их можно поставить перед JAR-файлами из `modules/shared`:

```java
ProjectLoaderInterface loader = ProjectV1LoaderFactory.create(preloadedClassLoaders);
```

## Быстрый старт: сохранение проекта

`ProjectV1SaverFactory` записывает JSON-файлы, при необходимости копирует JAR-файлы и всегда создаёт ZIP рядом с каталогом проекта. Возвращаемое значение — путь к архиву `<имя-каталога>.zip`.

```java
import io.github.byzatic.lib.configio.application.saver.ProjectSaverInterface;
import io.github.byzatic.lib.configio.infrastructure.factory.ProjectV1SaverFactory;

import java.nio.file.Path;
import java.util.List;

ProjectSaverInterface saver = ProjectV1SaverFactory.create();

Path archive = saver.save(
        projectDirectory,
        projectGlobal,
        nodeContainer,
        List.of(Path.of("plugins/MyRoutine.jar")),
        List.of(Path.of("plugins/MyService.jar"))
);
```

Также доступны перегрузки `save(ProjectLoadResultDataObject)` и `save(...)` без JAR-файлов.

## Формат каталога проекта

```text
MyProject/
├── data/
│   ├── Project.json
│   ├── Global.json
│   └── nodes/
│       └── <id>-<name>/
│           ├── global.json
│           └── pipeline.json
└── modules/
    ├── shared/
    ├── workflow_routines/
    └── services/
```

Для узла с `id == "#NAMED"` каталог называется только `<name>`, для остальных — `<id>-<name>`. Имя не должно позволять выйти за пределы `data/nodes`.

`Project.json` содержит имя, версию конфигурации и вложенное дерево узлов. Поддерживается ровно одно связное дерево: один корень, без циклов, повторных узлов и узлов с несколькими родителями.

## Основные интерфейсы

| Интерфейс | Назначение | Основные методы |
|---|---|---|
| `ProjectLoaderInterface` | Загрузка проекта из каталога | `load(Path)` |
| `ProjectSaverInterface` | Запись проекта и создание ZIP | четыре перегрузки `save(...)` |
| `ProjectArchiverInterface` | Архивация готового каталога | `archive(Path)` |
| `ModuleLoaderInterface` | Поиск и создание workflow-модулей | `getAvailableModuleNames()`, `getModule(...)`, `close()` |
| `ModuleSaverInterface` | Копирование JAR модуля | `save(moduleJar, projectDirectory)` |
| `ServiceLoaderInterface` | Поиск и создание сервисов | `getAvailableServiceNames()`, `getService(...)`, `close()` |
| `ServiceSaverInterface` | Копирование JAR сервиса | `save(serviceJar, projectDirectory)` |
| `ProjectRevisionSource` | Наблюдение за ZIP и публикация ревизий | `start(listener)`, `close()` |
| `ProjectRevisionListener` | Получение ревизии либо ошибки подготовки | `onRevisionAvailable(...)`, `onRevisionRejected(...)` |

Низкоуровневые DAO-контракты позволяют заменить JSON и class loader инфраструктуру:

| Интерфейс | Данные |
|---|---|
| `ProjectDaoInterface` | `Project.json` и структура графа |
| `ProjectGlobalDaoInterface` | `Global.json` |
| `NodeGlobalDaoInterface` | `global.json` каждого узла |
| `PipelineDaoInterface` | `pipeline.json` каждого узла |
| `SharedResourcesDaoInterface` | JAR-файлы из `modules/shared` |

Стандартные реализации создаются фабриками `ProjectV1LoaderFactory`, `ProjectV1SaverFactory`, `ModuleLoaderFactory`, `ModuleSaverFactory`, `ServiceLoaderFactory`, `ServiceSaverFactory` и `ProjectRevisionSourceFactory`.

## Основные классы

| Класс | Роль |
|---|---|
| `ProjectV1LoaderStrategy` | Координирует DAO, проверяет каталог и поддерживаемую версию, собирает `ProjectLoadResultDataObject` |
| `ProjectV1SaverStrategy` | Валидирует модель, записывает все части проекта, добавляет JAR и запускает архивацию |
| `GsonProjectDao` | Читает и пишет `data/Project.json` |
| `GsonProjectGlobalDao` | Читает и пишет `data/Global.json` |
| `GsonNodeGlobalDao` | Читает и пишет `global.json` узлов |
| `GsonPipelineDao` | Читает и пишет `pipeline.json` узлов |
| `UrlClassLoaderSharedResourcesDao` | Загружает отсортированные JAR-файлы из `modules/shared` в последовательную цепочку class loader'ов |
| `ModuleLoaderStrategy`, `ServiceLoaderStrategy` | Обнаруживают SPI-фабрики и создают экземпляры плагинов |
| `ModuleSaverStrategy`, `ServiceSaverStrategy` | Копируют JAR в соответствующие каталоги проекта |
| `ZipProjectArchiverStrategy` | Создаёт ZIP-архив каталога проекта |
| `PollingZipProjectRevisionSource` | Реализация polling-источника ZIP-ревизий |
| `ZipProjectRevisionSourceConfiguration` | Неизменяемая конфигурация путей, интервала и ограничений источника |
| `ProjectRevision` | Загруженный изолированный snapshot; закрывает проект и удаляет временный каталог |
| `ProjectRevisionFailure` | Описание отклонённой ревизии: архив, SHA-256 (если рассчитан) и причина |

Классы `infrastructure.factory` — рекомендуемые точки создания готовых реализаций. Прямое создание strategy/DAO имеет смысл при ручной сборке зависимостей или замене отдельных адаптеров.

## Доменная модель (DTO)

Публичная модель находится в пакете `io.github.byzatic.lib.configio.domain.model`. Коллекции в ней копируются при создании и возвращаются как неизменяемые.

| DTO | Содержимое |
|---|---|
| `ProjectLoadResultDataObject` | каталог проекта, глобальная конфигурация, контейнер узлов, общие ресурсы; реализует `AutoCloseable` |
| `ProjectDataObject` | `projectConfigVersion`, `projectName` |
| `ProjectStructureDataObject` | метаданные проекта и `Map<GraphNodeReferenceDataObject, NodeDataObject>` |
| `GraphNodeReferenceDataObject` | UUID-ссылка на узел; реализует value equality и используется ключом map |
| `NodeDataObject` | UUID, id, имя, описание и ссылки на downstream-узлы |
| `NodeContainerDataObject` | структура проекта, глобальные настройки и pipeline каждого узла |
| `ProjectGlobalDataObject` | глобальные хранилища и сервисы |
| `NodeGlobalDataObject` | хранилища конкретного узла |
| `StorageDataObject` | параметры, описание и `idName` хранилища |
| `ServiceDataObject` | параметры, описание и `idName` сервиса |
| `ConfigurationOptionDataObject` | `value`, `key`, `data`, `name` параметра |
| `PipelineDataObject` | порядок стадий и описания стадий |
| `StageConsistencyDataObject` | `stageId` и позиция стадии |
| `StageDescriptionDataObject` | `stageId` и список worker'ов |
| `WorkerDescriptionDataObject` | имя, описание и конфигурационные файлы worker'а |
| `ConfigurationFileDataObject` | описание и `configurationFileId` |
| `SharedResourcesContainerDataObject` | цепочка class loader'ов общих JAR; реализует `AutoCloseable` |

Связи основных DTO:

```text
ProjectLoadResultDataObject
├── ProjectGlobalDataObject
│   ├── StorageDataObject[]
│   └── ServiceDataObject[]
├── NodeContainerDataObject
│   ├── ProjectStructureDataObject
│   │   ├── ProjectDataObject
│   │   └── NodeDataObject[]
│   ├── NodeGlobalDataObject[]
│   └── PipelineDataObject[]
└── SharedResourcesContainerDataObject
```

Классы пакета `infrastructure.dto.raw` — технические Gson DTO, отражающие JSON-поля. Обычно пользователь библиотеки с ними напрямую не работает:

- `Project`, `GraphStructure` — `Project.json`;
- `Global`, `ServicesItem`, `StoragesItem`, `OptionsItem` — глобальная и узловая конфигурация;
- `NodeGlobal` — `global.json` узла;
- `Pipeline`, `StagesConsistencyItem`, `StagesDescriptionItem`, `WorkersDescriptionItem`, `ConfigurationFilesItem` — `pipeline.json`.

## Загрузка модулей и сервисов

JAR-плагины обнаруживаются стандартным Java SPI. JAR модуля должен регистрировать реализацию `WorkflowRoutineFactoryInterface`, а JAR сервиса — `ServiceFactoryInterface` в `META-INF/services/...`.

```java
try (ModuleLoaderInterface modules = ModuleLoaderFactory.create(
        projectDirectory.resolve("modules/workflow_routines"),
        project.getSharedResourcesContainer()
)) {
    System.out.println(modules.getAvailableModuleNames());
    WorkflowRoutineInterface routine = modules.getModule(
            "MyRoutine",
            routineApi,
            routineHealthFlag
    );
}
```

Имя плагина вычисляется из простого имени фабрики удалением суффикса `Factory`: `MyRoutineFactory` становится `MyRoutine`. Дублирующиеся имена считаются ошибкой. `ModuleLoaderInterface` и `ServiceLoaderInterface` владеют своими class loader'ами и должны закрываться.

## Отслеживание ревизий ZIP

`ProjectRevisionSourceFactory` создаёт polling-источник. Он ждёт несколько одинаковых наблюдений размера и времени изменения файла, вычисляет SHA-256, копирует и распаковывает архив в отдельный временный каталог, загружает проект и передаёт готовую ревизию listener'у.

```java
ZipProjectRevisionSourceConfiguration configuration =
        ZipProjectRevisionSourceConfiguration.newBuilder()
                .sourceArchive(Path.of("deploy/project.zip"))
                .stagingDirectory(Path.of("runtime/revisions"))
                .pollInterval(Duration.ofSeconds(1))
                .stableObservationCount(2)
                .maximumEntryCount(100_000)
                .maximumExpandedBytes(1024L * 1024L * 1024L)
                .build();

try (ProjectRevisionSource source = ProjectRevisionSourceFactory.create(configuration)) {
    source.start(new ProjectRevisionListener() {
        @Override
        public void onRevisionAvailable(ProjectRevision revision) {
            // Listener принимает владение ревизией.
            // Закрывать её можно только после остановки runtime, который её использует.
            activate(revision);
        }

        @Override
        public void onRevisionRejected(ProjectRevisionFailure failure) {
            failure.getCause().printStackTrace();
        }
    });

    awaitShutdown();
}
```

Callback'и выполняются последовательно в одном потоке источника и должны быстро возвращать управление. Получатель `ProjectRevision` отвечает за вызов `close()`: он закрывает ресурсы проекта и удаляет временный каталог ревизии. Ошибка новой ревизии не затрагивает ранее активированную ревизию.

Значения по умолчанию: интервал — 1 секунда, стабильных наблюдений — 2, максимум записей ZIP — 100 000, максимум распакованных данных — 1 GiB.

## Исключения

| Исключение | Когда возникает |
|---|---|
| `ProjectLoadingException` | неверный каталог, JSON, структура или версия проекта |
| `ProjectSavingException` | ошибка валидации, записи JSON, копирования плагина или архивации |
| `PluginLoadingException` | ошибка чтения JAR, SPI, дублирование или создание плагина |
| `PluginSavingException` | неверный JAR или ошибка его копирования |
| `ProjectRevisionException` | источник ревизий не стартует либо ZIP не проходит проверку/подготовку; во втором случае исключение приходит внутри `ProjectRevisionFailure` |

## Сборка и тесты

```shell
mvn test
```

Полная локальная сборка без подписи GPG:

```shell
mvn verify -Dgpg.skip=true
```

Интерактивный пример загрузчика расположен в `ProjectLoaderExampleApplication`.

## Лицензия

[Apache License 2.0](LICENSE).
