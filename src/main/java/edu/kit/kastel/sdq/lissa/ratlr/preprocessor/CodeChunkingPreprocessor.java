package edu.kit.kastel.sdq.lissa.ratlr.preprocessor;

import edu.kit.kastel.sdq.lissa.ratlr.Configuration;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Artifact;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;
import org.apache.jena.riot.Lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CodeChunkingPreprocessor extends Preprocessor {

    private final List<RecursiveSplitter.Language> languages;
    private final int chunkSize;

    public CodeChunkingPreprocessor(Configuration.ModuleConfiguration configuration) {
        this.languages = Arrays.stream(configuration.argumentAsString("language").split(",")).map(RecursiveSplitter.Language::valueOf).toList();
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

        Element artifactAsElement = new Element(artifact.getIdentifier(), artifact.getType(), artifact.getContent(), 0, null, false);
        elements.add(artifactAsElement);

        for (int i = 0; i < segments.size(); i++) {
            String segment = segments.get(i);
            Element segmentAsElement = new Element(artifact.getIdentifier() + SEPARATOR + i, artifact.getType(), segment, 1, artifactAsElement, true);
            elements.add(segmentAsElement);
        }

        return elements;
    }

    private List<String> generateSegments(Artifact artifact) {
        RecursiveSplitter.Language language = languages.size() == 1 ? languages.get(0) : getLanguage(artifact);
       return  RecursiveSplitter.fromLanguage(language, chunkSize).splitText(artifact.getContent());
    }

    private RecursiveSplitter.Language getLanguage(Artifact artifact) {
        String ending = artifact.getIdentifier().substring(artifact.getIdentifier().lastIndexOf(".") + 1);
        return switch (ending){
            case "java" -> RecursiveSplitter.Language.JAVA;
            case "py" -> RecursiveSplitter.Language.PYTHON;
            default -> throw new IllegalArgumentException("Unsupported language: " + ending);
        };
    }
}
