/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.preprocessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.kit.kastel.sdq.lissa.ratlr.configuration.ModuleConfiguration;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Artifact;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;

/**
 * This preprocessor splits the code into chunks of a given size.
 * Configuration:
 * <ul>
 * <li> language: the language of the code. Can be multiple languages separated by commas. If so, the file type is determined by the file ending. (i.e., the end
 * of ID of the artifact)
 * <li> chunk_size: the size of the chunks
 * </ul>
 */
public class CodeChunkingPreprocessor extends Preprocessor {

    private final List<RecursiveSplitter.Language> languages;
    private final int chunkSize;

    public CodeChunkingPreprocessor(ModuleConfiguration configuration) {
        this.languages = Arrays.stream(
                        configuration.argumentAsString("language").split(","))
                .map(RecursiveSplitter.Language::valueOf)
                .toList();
        this.chunkSize = configuration.argumentAsInt("chunk_size", 60);
    }

    @Override
    public List<Element> preprocess(List<Artifact> artifacts) {
        List<Element> elements = new ArrayList<>();
        for (Artifact artifact : artifacts) {
            List<Element> preprocessed = preprocess(artifact);
            elements.addAll(preprocessed);
        }
        return elements;
    }

    protected List<Element> preprocess(Artifact artifact) {

        List<String> segments = this.generateSegments(artifact);
        List<Element> elements = new ArrayList<>();

        Element artifactAsElement =
                new Element(artifact.getIdentifier(), artifact.getType(), artifact.getContent(), 0, null, false);
        elements.add(artifactAsElement);

        for (int i = 0; i < segments.size(); i++) {
            String segment = segments.get(i);
            Element segmentAsElement = new Element(
                    artifact.getIdentifier() + SEPARATOR + i, artifact.getType(), segment, 1, artifactAsElement, true);
            elements.add(segmentAsElement);
        }

        return elements;
    }

    private List<String> generateSegments(Artifact artifact) {
        RecursiveSplitter.Language language = languages.size() == 1 ? languages.getFirst() : getLanguage(artifact);
        return RecursiveSplitter.fromLanguage(language, chunkSize).splitText(artifact.getContent());
    }

    private RecursiveSplitter.Language getLanguage(Artifact artifact) {
        String ending =
                artifact.getIdentifier().substring(artifact.getIdentifier().lastIndexOf(".") + 1);
        return switch (ending) {
            case "java" -> RecursiveSplitter.Language.JAVA;
            case "py" -> RecursiveSplitter.Language.PYTHON;
            default -> throw new IllegalArgumentException("Unsupported language: " + ending);
        };
    }
}
