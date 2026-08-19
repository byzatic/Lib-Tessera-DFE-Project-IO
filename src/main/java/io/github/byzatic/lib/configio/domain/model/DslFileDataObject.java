package io.github.byzatic.lib.configio.domain.model;

import java.util.Objects;

/** DSL source attached to a project node. */
public final class DslFileDataObject {

    public static final String FILE_EXTENSION = ".mcg3dsl";

    private final GraphNodeReferenceDataObject nodeReference;
    private final String baseName;
    private final String content;

    public DslFileDataObject(
            GraphNodeReferenceDataObject nodeReference,
            String baseName,
            String content
    ) {
        this.nodeReference = Objects.requireNonNull(nodeReference, "nodeReference");
        if (baseName == null || baseName.isBlank()) {
            throw new IllegalArgumentException("DSL file base name must not be blank");
        }
        if (baseName.contains("/") || baseName.contains("\\")) {
            throw new IllegalArgumentException("DSL file base name must not contain path separators");
        }
        if (baseName.toLowerCase(java.util.Locale.ROOT).endsWith(FILE_EXTENSION)) {
            throw new IllegalArgumentException("DSL file base name must not include " + FILE_EXTENSION);
        }
        this.baseName = baseName;
        this.content = Objects.requireNonNull(content, "content");
    }

    public GraphNodeReferenceDataObject getNodeReference() {
        return nodeReference;
    }

    public String getBaseName() {
        return baseName;
    }

    public String getFileName() {
        return baseName + FILE_EXTENSION;
    }

    public String getContent() {
        return content;
    }
}
