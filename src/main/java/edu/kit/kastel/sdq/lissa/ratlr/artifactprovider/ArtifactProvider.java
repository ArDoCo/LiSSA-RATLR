/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.artifactprovider;

import java.util.List;

import edu.kit.kastel.sdq.lissa.ratlr.configuration.ModuleConfiguration;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Artifact;

/**
 * Provides artifacts for LiSSA approach.
 */
public abstract class ArtifactProvider {

    /**
     * Returns the artifacts provided by this provider.
     */
    public abstract List<Artifact> getArtifacts();

    public abstract Artifact getArtifact(String identifier);

    public static ArtifactProvider createArtifactProvider(ModuleConfiguration configuration) {
        return switch (configuration.name()) {
            case "text" -> new TextArtifactProvider(configuration);
            case "recursive_text" -> new RecursiveTextArtifactProvider(configuration);
            default -> throw new IllegalStateException("Unexpected value: " + configuration.name());
        };
    }
}
