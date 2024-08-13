package edu.kit.kastel.sdq.lissa.ratlr.artifactprovider;

import edu.kit.kastel.sdq.lissa.ratlr.Configuration;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Artifact;

import java.util.List;

public abstract class ArtifactProvider {
    public abstract List<Artifact> getArtifacts();

    public abstract Artifact getArtifact(String identifier);

    public static ArtifactProvider createArtifactProvider(Configuration.ModuleConfiguration configuration) {
        return switch (configuration.name()) {
            case "text" -> new TextArtifactProvider(configuration);
            case "recursive_text" -> new RecursiveTextArtifactProvider(configuration);
            default -> throw new IllegalStateException("Unexpected value: " + configuration.name());
        };
    }

}
