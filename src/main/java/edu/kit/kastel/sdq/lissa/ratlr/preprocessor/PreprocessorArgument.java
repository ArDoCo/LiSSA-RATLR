package edu.kit.kastel.sdq.lissa.ratlr.preprocessor;

public enum PreprocessorArgument {
    
    LANGUAGE,
    CHUNK_SIZE,
    COMPARE_CLASSES,
    INCLUDE_USAGES,
    INCLUDE_OPERATIONS,
    INCLUDE_INTERFACE_REALIZATIONS,
    COUNT,
    TEMPLATE,
    MODEL,
    SEED,
    TEMPERATURE,
    TYPE;

    public String getKey() {
        return switch (this) {
            case INCLUDE_USAGES -> "includeUsages";
            case INCLUDE_OPERATIONS -> "includeOperations";
            case INCLUDE_INTERFACE_REALIZATIONS -> "includeInterfaceRealizations";
            default -> name().toLowerCase();
        };
    }
}
