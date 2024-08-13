package edu.kit.kastel.sdq.lissa.ratlr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@RecordBuilder()
public record Configuration(@JsonProperty("cache_dir") String cacheDir,
                            @JsonProperty("gold_standard_configuration") GoldStandardConfiguration goldStandardConfiguration,
                            @JsonProperty("source_artifact_provider") ModuleConfiguration sourceArtifactProvider,
                            @JsonProperty("target_artifact_provider") ModuleConfiguration targetArtifactProvider,
                            @JsonProperty("source_preprocessor") ModuleConfiguration sourcePreprocessor,
                            @JsonProperty("target_preprocessor") ModuleConfiguration targetPreprocessor,
                            @JsonProperty("embedding_creator") ModuleConfiguration embeddingCreator,
                            @JsonProperty("source_store") ModuleConfiguration sourceStore, @JsonProperty("target_store") ModuleConfiguration targetStore,
                            @JsonProperty("classifier") ModuleConfiguration classifier, @JsonProperty("result_aggregator") ModuleConfiguration resultAggregator,
                            @JsonProperty("tracelinkid_postprocessor") ModuleConfiguration traceLinkIdPostprocessor) implements ConfigurationBuilder.With {

    public String serializeAndDestroyConfiguration() throws IOException {
        sourceArtifactProvider.finalizeForSerialization();
        targetArtifactProvider.finalizeForSerialization();
        sourcePreprocessor.finalizeForSerialization();
        targetPreprocessor.finalizeForSerialization();
        embeddingCreator.finalizeForSerialization();
        sourceStore.finalizeForSerialization();
        targetStore.finalizeForSerialization();
        classifier.finalizeForSerialization();
        resultAggregator.finalizeForSerialization();
        if (traceLinkIdPostprocessor != null) {
            traceLinkIdPostprocessor.finalizeForSerialization();
        }
        return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsString(this);
    }

    @Override
    public String toString() {
        return "Configuration{" + "sourceArtifactProvider=" + sourceArtifactProvider + ", targetArtifactProvider=" + targetArtifactProvider + ", sourcePreprocessor=" + sourcePreprocessor + ", targetPreprocessor=" + targetPreprocessor + ", embeddingCreator=" + embeddingCreator + ", sourceStore=" + sourceStore + ", targetStore=" + targetStore + ", classifier=" + classifier + ", resultAggregator=" + resultAggregator + ", traceLinkIdPostprocessor=" + traceLinkIdPostprocessor + '}';
    }

    public record GoldStandardConfiguration(@JsonProperty("path") String path, @JsonProperty(defaultValue = "false") boolean hasHeader) {
    }

    public static final class ModuleConfiguration {
        @JsonProperty("name")
        private final String name;
        @JsonProperty("args")
        private final Map<String, String> arguments;

        @JsonIgnore
        private final Map<String, String> retrievedArguments = new LinkedHashMap<>();

        @JsonIgnore
        private boolean finalized = false;

        @JsonCreator
        public ModuleConfiguration(@JsonProperty("name") String name, @JsonProperty("args") Map<String, String> arguments) {
            this.name = name;
            this.arguments = arguments;
        }

        public String name() {
            return name;
        }

        public boolean hasArgument(String key) {
            return this.arguments.get(key) != null;
        }

        public String argumentAsString(String key) {
            if (finalized) {
                throw new IllegalStateException("Configuration already finalized for serialization");
            }

            String argument = arguments.get(key);
            if (argument == null) {
                throw new IllegalArgumentException("Argument with key " + key + " not found in configuration " + this);
            }
            retrievedArguments.put(key, argument);
            return argument;
        }

        public String argumentAsString(String key, String defaultValue) {
            if (finalized) {
                throw new IllegalStateException("Configuration already finalized for serialization");
            }

            String argument = arguments.getOrDefault(key, defaultValue);
            String retrievedArgument = retrievedArguments.put(key, argument);
            if (retrievedArgument != null && !retrievedArgument.equals(argument)) {
                throw new IllegalArgumentException(
                        "Default argument for key " + key + " already set to " + retrievedArgument + " and cannot be changed to " + defaultValue);
            }
            return argument;
        }

        public int argumentAsInt(String key) {
            return Integer.parseInt(argumentAsString(key));
        }

        public int argumentAsInt(String key, int defaultValue) {
            return Integer.parseInt(argumentAsString(key, String.valueOf(defaultValue)));
        }

        public boolean argumentAsBoolean(String key) {
            return Boolean.parseBoolean(argumentAsString(key));
        }

        public boolean argumentAsBoolean(String key, boolean defaultValue) {
            return Boolean.parseBoolean(argumentAsString(key, String.valueOf(defaultValue)));
        }

        void finalizeForSerialization() {
            if (finalized) {
                return;
            }

            finalized = true;
            arguments.putAll(retrievedArguments);

            for (var argumentKey : arguments.keySet()) {
                if (!retrievedArguments.containsKey(argumentKey)) {
                    throw new IllegalStateException("Argument with key " + argumentKey + " not retrieved from configuration " + this);
                }
            }

        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || obj.getClass() != this.getClass())
                return false;
            var that = (ModuleConfiguration) obj;
            return Objects.equals(this.name, that.name) && Objects.equals(this.arguments, that.arguments);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, arguments);
        }

        @Override
        public String toString() {
            return "ModuleConfiguration[name=" + name + ", arguments=" + arguments + ']';
        }

    }
}
