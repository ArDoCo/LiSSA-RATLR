/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.preprocessor;

import edu.kit.kastel.sdq.lissa.ratlr.configuration.ModuleConfiguration;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Artifact;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;
import edu.kit.kastel.sdq.lissa.ratlr.preprocessor.reformulate.Reformulation;

import java.util.List;

/**
 * A preprocessor extracts elements based on the given artifacts.
 */
public abstract class Preprocessor {
    public static final String SEPARATOR = "$";

    public abstract List<Element> preprocess(List<Artifact> artifacts);

    public static Preprocessor createPreprocessor(ModuleConfiguration configuration) {
        return switch (configuration.name()) {
            case "sentence" -> new SentencePreprocessor();
            case "reformulation_openai" -> Reformulation.createPreprocessor(configuration);
            case "code_chunking" -> new CodeChunkingPreprocessor(configuration);
            case "code_method" -> new CodeMethodPreprocessor(configuration);
            case "code_tree" -> new CodeTreePreprocessor(configuration);
            case "model_uml" -> new ModelUMLPreprocessor(configuration);
            case "artifact" -> new SingleArtifactPreprocessor();
            default -> throw new IllegalStateException("Unexpected value: " + configuration.name());
        };
    }
}
