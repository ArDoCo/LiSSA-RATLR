package edu.kit.kastel.sdq.lissa.ratlr.preprocessor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.model.chat.ChatLanguageModel;
import edu.kit.kastel.sdq.lissa.ratlr.Configuration;
import edu.kit.kastel.sdq.lissa.ratlr.cache.Cache;
import edu.kit.kastel.sdq.lissa.ratlr.cache.CacheManager;
import edu.kit.kastel.sdq.lissa.ratlr.classifier.ChatLanguageModelProvider;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Artifact;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;
import edu.kit.kastel.sdq.lissa.ratlr.utils.KeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SentenceSummaryPreprocessor extends Preprocessor {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String DEFAULT_TEMPLATE =
            """
            Your task is to find {count} sentences which are similar to the following one:

            '''{source_content}'''

            Answer by wrapping the found sentences into the following JSON format, nothing else.
            JSON format: '''{"similar_sentences":[]}
            """;

    private final Cache cache;
    private final ChatLanguageModel llm;
    private final String template;
    private final int count;

    public SentenceSummaryPreprocessor(Configuration.ModuleConfiguration configuration) {
        ChatLanguageModelProvider provider = new ChatLanguageModelProvider(configuration, configuration.argumentAsDouble("temperature", 0.0));
        this.template = configuration.argumentAsString("template", DEFAULT_TEMPLATE);
        this.cache = CacheManager.getDefaultInstance()
                .getCache(this.getClass().getSimpleName() + "_" + provider.modelName());
        this.llm = provider.createChatModel();
        this.count = configuration.argumentAsInt("count", 5);
    }

    private String summarize(Element base) {
        String request = template.replace("{count}", String.valueOf(count))
                .replace("{source_content}", base.getContent());

        String key = KeyGenerator.generateKey(request);
        String cachedResponse = cache.get(key, String.class);
        if (cachedResponse != null) {
            return cachedResponse;
        } else {
            logger.info("Summarizing: {}", base.getIdentifier());
            String response = llm.generate(request);
            cache.put(key, response);
            System.out.println(response);
            return response;
        }
    }

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
            try {
                elements.addAll(preprocessSentence(sentence, artifact, i, artifactAsElement));
            } catch (JsonProcessingException e) {
                logger.warn("Failed to parse sentence: '{}' with exception: {}", sentence, e.getMessage());
            }
        }
        return elements;
    }
    
    private List<Element> preprocessSentence(String sentence, Artifact artifact, int sentenceId, Element parent) throws JsonProcessingException {
        List<Element> elements = new ArrayList<>();
        Element sentenceAsElement = new Element(
                artifact.getIdentifier() + SEPARATOR + sentenceId, artifact.getType(), sentence, 1, parent, true);
        elements.add(sentenceAsElement);
        String summariyJson = summarize(sentenceAsElement).replace("```json", "").replace("```", "");
        SentenceSummary summary = new ObjectMapper().readValue(summariyJson, SentenceSummary.class);
        for (int i = 0; i < summary.getSentences().size(); i++) {
            elements.add(new Element(
                    artifact.getIdentifier() + SEPARATOR + sentenceId + SEPARATOR + i, artifact.getType(), summary.getSentences().get(i), 2, null, true));
        }
        return elements;
    }
    
    private static final class SentenceSummary {
        
        @JsonProperty("similar_sentences")
        private final List<String> sentences;

        @JsonCreator
        private SentenceSummary(@JsonProperty("similar_sentences") List<String> sentences) {
            this.sentences = sentences;
        }
        
        public List<String> getSentences() {
            return sentences;
        }
    }
}
