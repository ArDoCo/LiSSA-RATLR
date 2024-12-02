package edu.kit.kastel.sdq.lissa.ratlr.preprocessor;

import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Artifact;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * This preprocessor splits a text into sentences.
 */
public class SentencePreprocessor extends Preprocessor {

    @Override
    public List<Element> preprocess(List<Artifact> artifacts) {
        List<Element> elements = new ArrayList<>();
        for (Artifact artifact : artifacts) {
            List<Element> preprocessed = preprocessIntern(artifact);
            elements.addAll(preprocessed);
        }
        return elements;
    }

    private List<Element> preprocessIntern(Artifact artifact) {
        DocumentBySentenceSplitter splitter = new DocumentBySentenceSplitter(Integer.MAX_VALUE, 0);
        String[] sentences = splitter.split(artifact.getContent());
        List<Element> elements = new ArrayList<>();

        Element artifactAsElement =
                new Element(artifact.getIdentifier(), artifact.getType(), artifact.getContent(), 0, null, false);
        elements.add(artifactAsElement);

        for (int i = 0; i < sentences.length; i++) {
            String sentence = sentences[i];
            elements.addAll(createElements(sentence, artifact, i, artifactAsElement));
        }
        return elements;
    }
    
    protected List<Element> createElements(String sentence, Artifact artifact, int sentenceId, Element parent) {
        return List.of(new Element(artifact.getIdentifier() + SEPARATOR + sentenceId, artifact.getType(), sentence, 1, parent, true));
    }
}
