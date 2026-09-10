# Unified API

Публичный API библиотеки находится в пакете
`io.github.byzatic.tessera.lib.configio.unified`. Основная точка входа — `TesseraProjectIO`:

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

Дополнительные файлы передаются в сохранение и экспорт через `ProjectArtifacts`:

```java
ProjectArtifacts artifacts = ProjectArtifacts.newBuilder()
        .sharedJars(sharedJars)
        .routineJars(routineJars)
        .serviceJars(serviceJars)
        .dslSources(dslSources)
        .build();
```

`sharedJars` сохраняются в `modules/shared/`, `routineJars` — в
`modules/workflow_routines/`, `serviceJars` — в `modules/services/`. Все они включаются в
создаваемый ZIP.

Исполняемые плагины и их метаданные доступны через закрываемую `ProjectRuntimeSession`:

```java
try (ProjectRuntimeSession runtime = projectIO.openRuntime(projectDirectory)) {
    runtime.getAvailableRoutineNames();
    runtime.getAvailableServiceNames();
    runtime.getRoutineMetadata();
    runtime.getServiceMetadata();
}
```

ZIP-ревизии отслеживаются через `TesseraProjectIO.watchRevisions(...)`. Возвращаемая
`ProjectRevisionSubscription`, каждый полученный `ProjectRevisionHandle` и открытая runtime-сессия
имеют явный жизненный цикл и должны закрываться владельцем.

Пользовательские модели находятся в `unified.model`, являются неизменяемыми и создаются через
`newBuilder()`. SPI редакторских метаданных находится в `unified.spi.routine` и
`unified.spi.service`. Содержимое `unified.internal` является деталью реализации.

Полная документация и примеры находятся в корневом [README](../../../../../../../../../../README.md).
