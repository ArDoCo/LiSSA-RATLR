/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.preprocessor;

import java.util.List;

import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Artifact;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;

/**
 * This preprocessor defines the artifact as element.
 */
public class SingleArtifactPreprocessor extends Preprocessor {
    @Override
    public List<Element> preprocess(List<Artifact> artifacts) {
        return artifacts.stream()
                .map(artifact ->
                        new Element(artifact.getIdentifier(), artifact.getType(), artifact.getContent(), 0, null, true))
                .toList();
    }
}
