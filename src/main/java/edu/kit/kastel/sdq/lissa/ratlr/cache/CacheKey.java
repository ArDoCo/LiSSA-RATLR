/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.cache;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CacheKey(String model, int seed, Mode mode, String content, @JsonIgnore String localKey) {
    public enum Mode {
        EMBEDDING,
        CHAT
    }

    private static final ObjectMapper MAPPER = new ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true);

    public String toRawKey() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not serialize key", e);
        }
    }
}
