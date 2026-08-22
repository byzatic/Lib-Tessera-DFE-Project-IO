package io.github.byzatic.tessera.lib.configio.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StageDescriptionDataObject {

    private final List<WorkerDescriptionDataObject> workers;
    private final String stageId;

    public StageDescriptionDataObject(
            List<WorkerDescriptionDataObject> workers,
            String stageId
    ) {
        if (workers == null) {
            this.workers = Collections.emptyList();
        } else {
            this.workers = Collections.unmodifiableList(
                    new ArrayList<WorkerDescriptionDataObject>(workers)
            );
        }
        this.stageId = stageId;
    }

    public List<WorkerDescriptionDataObject> getWorkers() {
        return workers;
    }

    public String getStageId() {
        return stageId;
    }
}
