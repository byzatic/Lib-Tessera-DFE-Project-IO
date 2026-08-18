package io.github.byzatic.lib.configio.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class NodeDataObject {

    private final String uuid;
    private final String id;
    private final String name;
    private final String description;
    private final List<GraphNodeReferenceDataObject> downstream;

    public NodeDataObject(
            String uuid,
            String id,
            String name,
            String description,
            List<GraphNodeReferenceDataObject> downstream
    ) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.description = description;
        Objects.requireNonNull(downstream, "downstream");
        this.downstream = Collections.unmodifiableList(
                new ArrayList<GraphNodeReferenceDataObject>(downstream)
        );
    }

    public String getUuid() {
        return uuid;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<GraphNodeReferenceDataObject> getDownstream() {
        return downstream;
    }
}
