/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.preprocessor;

import java.util.ArrayList;
import java.util.List;

import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import edu.kit.kastel.sdq.lissa.ratlr.configuration.ModuleConfiguration;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Artifact;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;

/**
 * This preprocessor splits a text into sentences.
 */
public class SentencePreprocessor extends Preprocessor {
    public SentencePreprocessor(ModuleConfiguration configuration) {}

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
            Element sentenceAsElement = new Element(
                    artifact.getIdentifier() + SEPARATOR + i, artifact.getType(), sentence, 1, artifactAsElement, true);
            elements.add(sentenceAsElement);
        }
        return elements;
    }
}
