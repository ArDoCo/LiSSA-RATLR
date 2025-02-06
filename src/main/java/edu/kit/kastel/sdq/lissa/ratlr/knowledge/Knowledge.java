/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.knowledge;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Artifact.class, name = "artifact"),
    @JsonSubTypes.Type(value = Element.class, name = "element")
})
public abstract sealed class Knowledge permits Artifact, Element {
    @JsonProperty
    private final String identifier;

    @JsonProperty
    private final String type;

    @JsonProperty
    private final String content;

    @JsonIgnore
    private final String normalizedContent;

    protected Knowledge(String identifier, String type, String content) {
        this.identifier = identifier;
        this.type = type;
        this.content = content;
        this.normalizedContent = content.replace("\r\n", "\n");
    }

    public final String getContent() {
        // We do want to return the normalized content here
        return normalizedContent;
    }

    public final String getIdentifier() {
        return identifier;
    }

    public final String getType() {
        return type;
    }
}
