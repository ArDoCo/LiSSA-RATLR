/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.artifactprovider;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.apache.commons.io.IOUtils;

import edu.kit.kastel.sdq.lissa.ratlr.configuration.ModuleConfiguration;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Artifact;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Knowledge;

/**
 * Provides text-based artifact for a configured path. The filename is used as identifier.
 * Configuration:
 * <ul>
 * <li> path: the path to the file
 * <li> artifact_type: the type of the artifact
 * </ul>
 */
public class TextArtifactProvider extends ArtifactProvider {

    protected final File path;
    protected final Artifact.ArtifactType artifactType;
    protected final List<Artifact> artifacts;

    public TextArtifactProvider(ModuleConfiguration configuration) {
        this.path = new File(configuration.argumentAsString("path"));
        if (!path.exists()) {
            throw new IllegalArgumentException("Path does not exist: " + path.getAbsolutePath());
        }
        this.artifactType = Artifact.ArtifactType.from(configuration.argumentAsString("artifact_type"));
        this.artifacts = new ArrayList<>();
    }

    protected void loadFiles() {
        List<File> files = new ArrayList<>();
        if (this.path.isFile()) {
            files.add(this.path);
        } else {
            files.addAll(Arrays.asList(Objects.requireNonNull(this.path.listFiles())));
        }

        for (File file : files) {
            Path it = file.toPath();
            if (Files.isRegularFile(it)) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try (BufferedReader reader = new BufferedReader(new FileReader(it.toFile()))) {
                    IOUtils.copy(reader, bos, StandardCharsets.UTF_8);
                    String content = bos.toString(StandardCharsets.UTF_8);
                    artifacts.add(new Artifact(it.getFileName().toString(), artifactType, content));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
    }

    @Override
    public List<Artifact> getArtifacts() {
        if (artifacts.isEmpty()) this.loadFiles();
        var orderedArtifacts = new ArrayList<>(this.artifacts);
        orderedArtifacts.sort(Comparator.comparing(Knowledge::getIdentifier));
        return orderedArtifacts;
    }

    @Override
    public Artifact getArtifact(String identifier) {
        if (artifacts.isEmpty()) this.loadFiles();
        return artifacts.stream()
                .filter(it -> it.getIdentifier().equals(identifier))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Artifact not found"));
    }
}
