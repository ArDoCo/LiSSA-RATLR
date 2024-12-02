package edu.kit.kastel.sdq.lissa.ratlr.preprocessor.reformulate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.kit.kastel.sdq.lissa.ratlr.preprocessor.SentencePreprocessor;

import edu.kit.kastel.sdq.lissa.ratlr.Configuration;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Artifact;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;

/**
 * This preprocessor splits a text into sentences and reformulates those to similar ones.
 * Configuration:
 * <ul>
 * <li> count: the count of requested reformulations per sentence
 * <li> template: the template on which the request is based on. The literals {@code {count}} and {@code {source_content}} are considered placeholders
 * <li> model: the chat language model to use for reformulation
 * <li> seed: the seed of the model
 * <li> temperature: the temperature of the model
 * </ul>
 */
public class SentenceReformulationPreprocessor extends SentencePreprocessor {

    private static final String REFORMULATED_OUTPUT_PREFIX = "- ";
    private static final String DEFAULT_TEMPLATE_LIST =
            """
            Your task is to find {count} sentences which are similar to the following one:

            '''{source_content}'''
            
            Output format:
            {outputPrefix} similar sentence 1
            {outputPrefix} similar sentence 2
            """.replace("{outputPrefix}", REFORMULATED_OUTPUT_PREFIX);
    private final ArtifactReformulator reformulator;

    public SentenceReformulationPreprocessor(Configuration.ModuleConfiguration configuration) {
        this.reformulator = new ArtifactReformulator(configuration, DEFAULT_TEMPLATE_LIST) {
            @Override
            protected List<String> collectResult(String response) {
                return Arrays.stream(response.trim().split("\n"))
                        .filter(line -> line.startsWith(REFORMULATED_OUTPUT_PREFIX))
                        .map(line -> line.substring(REFORMULATED_OUTPUT_PREFIX.length(), line.length() - 1))
                        .toList();
            }
        };
    }

    @Override
    protected List<Element> createElements(String sentence, Artifact artifact, int sentenceId, Element parent) {
        List<Element> elements = new ArrayList<>();
        Element sentenceAsElement = new Element(
                artifact.getIdentifier() + SEPARATOR + sentenceId, artifact.getType(), sentence, 1, parent, false);
        elements.add(sentenceAsElement);
        elements.add(new Element(artifact.getIdentifier() + SEPARATOR + sentenceId + SEPARATOR + 0, artifact.getType(), sentence, 2, sentenceAsElement, true));
        List<String> reformulated = reformulator.reformulate(sentenceAsElement.getContent(), sentenceAsElement.getIdentifier());
        for (int i = 0; i < reformulated.size(); i++) {
            elements.add(new Element(
                    artifact.getIdentifier() + SEPARATOR + sentenceId + SEPARATOR + (i + 1),
                    artifact.getType(),
                    reformulated.get(i),
                    2,
                    sentenceAsElement,
                    true));
        }
        return elements;
    }
}
