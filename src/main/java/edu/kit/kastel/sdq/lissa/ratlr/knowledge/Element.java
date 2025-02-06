/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.knowledge;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Element extends Knowledge {
    @JsonProperty
    private final int granularity;

    @JsonIgnore
    private Element parent;

    @JsonProperty
    private final String parentId;

    @JsonProperty
    private final boolean compare;

    @JsonCreator
    private Element(
            @JsonProperty("identifier") String identifier,
            @JsonProperty("type") String type,
            @JsonProperty("content") String content,
            @JsonProperty("granularity") int granularity,
            @JsonProperty("parentId") String parentId,
            @JsonProperty("compare") boolean compare) {
        super(identifier, type, content);
        this.granularity = granularity;
        this.parentId = parentId;
        this.compare = compare;
    }

    public Element(String identifier, String type, String content, int granularity, Element parent, boolean compare) {
        super(identifier, type, content);
        this.granularity = granularity;
        this.parentId = parent == null ? null : parent.getIdentifier();
        this.parent = parent;
        this.compare = compare;
    }

    public void init(Map<String, Element> otherKnowledge) {
        if (parentId != null) {
            parent = otherKnowledge.get(parentId);
        }
    }

    public int getGranularity() {
        return granularity;
    }

    public Element getParent() {
        return parent;
    }

    public boolean isCompare() {
        return compare;
    }
}
