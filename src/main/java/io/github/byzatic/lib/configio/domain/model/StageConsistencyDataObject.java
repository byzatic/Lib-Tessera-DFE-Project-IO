package io.github.byzatic.lib.configio.domain.model;

public final class StageConsistencyDataObject {

    private final String stageId;
    private final Integer position;

    public StageConsistencyDataObject(String stageId, Integer position) {
        this.stageId = stageId;
        this.position = position;
    }

    public String getStageId() {
        return stageId;
    }

    public Integer getPosition() {
        return position;
    }
}
