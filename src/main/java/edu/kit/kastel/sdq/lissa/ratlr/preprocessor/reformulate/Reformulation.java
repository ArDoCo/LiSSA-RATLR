package edu.kit.kastel.sdq.lissa.ratlr.preprocessor.reformulate;

import edu.kit.kastel.sdq.lissa.ratlr.configuration.ModuleConfiguration;
import edu.kit.kastel.sdq.lissa.ratlr.preprocessor.Preprocessor;
import edu.kit.kastel.sdq.lissa.ratlr.preprocessor.PreprocessorArgument;

public final class Reformulation {

    private Reformulation() {
        // utility class
    }

    public static Preprocessor createPreprocessor(ModuleConfiguration configuration) {
        return switch (configuration.argumentAsString(PreprocessorArgument.TYPE.getKey())) {
            case "sentence" -> new SentenceReformulationPreprocessor(configuration);
            default -> throw new IllegalStateException("Unexpected value: " + PreprocessorArgument.TYPE.getKey());
        };
    }
}
