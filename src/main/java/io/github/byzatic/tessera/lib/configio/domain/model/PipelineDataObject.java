package io.github.byzatic.tessera.lib.configio.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PipelineDataObject {

    private final List<StageConsistencyDataObject> stagesConsistency;
    private final List<StageDescriptionDataObject> stagesDescription;

    public PipelineDataObject(
            List<StageConsistencyDataObject> stagesConsistency,
            List<StageDescriptionDataObject> stagesDescription
    ) {
        this.stagesConsistency = immutableList(stagesConsistency);
        this.stagesDescription = immutableList(stagesDescription);
    }

    public List<StageConsistencyDataObject> getStagesConsistency() {
        return stagesConsistency;
    }

    public List<StageDescriptionDataObject> getStagesDescription() {
        return stagesDescription;
    }

    private <T> List<T> immutableList(List<T> source) {
        if (source == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<T>(source));
    }
}
