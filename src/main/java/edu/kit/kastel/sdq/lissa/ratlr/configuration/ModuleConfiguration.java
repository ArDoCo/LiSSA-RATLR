/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.configuration;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class ModuleConfiguration {
    public static final String ALREADY_FINALIZED_FOR_SERIALIZATION =
            "Configuration already finalized for serialization";

    @JsonProperty("name")
    private final String name;

    @JsonProperty("args")
    private final Map<String, String> arguments;

    // This field is used to store the retrieved arguments for serialization
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
            throw new IllegalStateException(ALREADY_FINALIZED_FOR_SERIALIZATION);
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
            throw new IllegalStateException(ALREADY_FINALIZED_FOR_SERIALIZATION);
        }

        String argument = arguments.getOrDefault(key, defaultValue);
        String retrievedArgument = retrievedArguments.put(key, argument);
        if (retrievedArgument != null && !retrievedArgument.equals(argument)) {
            throw new IllegalArgumentException("Default argument for key " + key + " already set to "
                    + retrievedArgument + " and cannot be changed to " + defaultValue);
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

    public <E extends Enum<E>> String argumentAsStringByEnumIndex(
            String key, int defaultIndex, E[] values, Function<E, String> transform) {
        if (finalized) {
            throw new IllegalStateException(ALREADY_FINALIZED_FOR_SERIALIZATION);
        }

        String value = arguments.getOrDefault(key, String.valueOf(defaultIndex));
        // If not a number, it can be the text itself
        try {
            int index = Integer.parseInt(value);
            if (index < 0 || index >= values.length) {
                throw new IllegalArgumentException(
                        "Index " + index + " out of bounds for enum " + Arrays.toString(values));
            }
            value = transform.apply(values[index]);
        } catch (NumberFormatException e) {
            // It's not a number, so it's the text itself
        }

        String retrievedArgument = retrievedArguments.put(key, value);
        if (retrievedArgument != null && !retrievedArgument.equals(value)) {
            throw new IllegalArgumentException("Default argument for key " + key + " already set to "
                    + retrievedArgument + " and cannot be changed to " + value);
        }
        return value;
    }

    void finalizeForSerialization() {
        if (finalized) {
            return;
        }

        finalized = true;
        arguments.putAll(retrievedArguments);

        for (var argumentKey : arguments.keySet()) {
            if (!retrievedArguments.containsKey(argumentKey)) {
                throw new IllegalStateException(
                        "Argument with key " + argumentKey + " not retrieved from configuration " + this);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
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
