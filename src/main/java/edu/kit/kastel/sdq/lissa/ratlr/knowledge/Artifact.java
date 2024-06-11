package edu.kit.kastel.sdq.lissa.ratlr.knowledge;

import com.fasterxml.jackson.annotation.JsonCreator;

public final class Artifact extends Knowledge {

    public Artifact(String identifier, ArtifactType type, String content) {
        super(identifier, type.toString().replace("_", " ").toLowerCase(), content);
    }

    @JsonCreator
    public Artifact(String identifier, String type, String content) {
        super(identifier, type, content);
    }

    public enum ArtifactType {
        TEXT, SOURCE_CODE, CODE, REQUIREMENT;

        public static ArtifactType from(String type) {
            for (ArtifactType artifactType : values()) {
                if (artifactType.toString().equalsIgnoreCase(type.replace(" ", "_"))) {
                    return artifactType;
                }
            }
            throw new IllegalArgumentException("Unknown artifact type '" + type + "'");
        }
    }
}
