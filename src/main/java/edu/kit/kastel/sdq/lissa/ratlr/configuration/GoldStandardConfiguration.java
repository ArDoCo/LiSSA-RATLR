/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.configuration;

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public record GoldStandardConfiguration(
        @JsonProperty("path") String path, @JsonProperty(defaultValue = "false") boolean hasHeader) {

    public static GoldStandardConfiguration load(Path evaluationConfig) {
        if (evaluationConfig == null) return null;

        try {
            return new ObjectMapper().readValue(evaluationConfig.toFile(), GoldStandardConfiguration.class);
        } catch (IOException e) {
            LoggerFactory.getLogger(GoldStandardConfiguration.class)
                    .error("Loading evaluation config threw an exception: {}", e.getMessage());
            return null;
        }
    }
}
