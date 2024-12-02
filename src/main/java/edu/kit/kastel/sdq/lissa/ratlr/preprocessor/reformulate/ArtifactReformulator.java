package edu.kit.kastel.sdq.lissa.ratlr.preprocessor.reformulate;

import dev.langchain4j.model.chat.ChatLanguageModel;
import edu.kit.kastel.sdq.lissa.ratlr.Configuration;
import edu.kit.kastel.sdq.lissa.ratlr.cache.Cache;
import edu.kit.kastel.sdq.lissa.ratlr.cache.CacheManager;
import edu.kit.kastel.sdq.lissa.ratlr.classifier.ChatLanguageModelProvider;
import edu.kit.kastel.sdq.lissa.ratlr.preprocessor.PreprocessorArgument;
import edu.kit.kastel.sdq.lissa.ratlr.utils.KeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Configuration:
 * <ul>
 * <li> count: the count of requested reformulations per sentence
 * <li> template: the template on which the request is based on. The literals {@code {count}} and {@code {source_content}} are considered placeholders
 * <li> model: the chat language model to use for reformulation
 * <li> seed: the seed of the model
 * <li> temperature: the temperature of the model
 * </ul>
 */
public abstract class ArtifactReformulator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactReformulator.class);

    private final Cache cache;
    private final ChatLanguageModel llm;
    private final String template;
    private final int count;

    /**
     * Creates a new reformulator. The literals {@code {count}} and {@code {source_content}} are considered placeholders in the template 
     * and will be replaced.
     * @param configuration the configuration of this reformulator
     * @param defaultTemplate the default template to use if none is specified by the configuration
     */
    protected ArtifactReformulator(Configuration.ModuleConfiguration configuration, String defaultTemplate) {
        this.count = configuration.argumentAsInt(PreprocessorArgument.COUNT.getKey(), 5);
        this.template = configuration.argumentAsString(PreprocessorArgument.TEMPLATE.getKey(), defaultTemplate)
                .replace("{count}", String.valueOf(count));
        ChatLanguageModelProvider provider =
                new ChatLanguageModelProvider(configuration, configuration.argumentAsDouble(PreprocessorArgument.TEMPERATURE.getKey(), 0.0));
        this.cache = CacheManager.getDefaultInstance()
                .getCache(ArtifactReformulator.class.getSimpleName() + "_" + provider.modelName());
        this.llm = provider.createChatModel();
    }

    /**
     * Reformulates the content with the configured language model. Uses cached response instead if present.
     * @param content the content that replaces {@code {source_content}} in the template for the request
     * @param identifier the identifier of the content, for logging purposes
     * @return the collected reformulated result
     */
    public List<String> reformulate(String content, String identifier) {
        String request = template.replace("{source_content}", content);

        String key = KeyGenerator.generateKey(request);
        String cachedResponse = cache.get(key, String.class);
        
        List<String> collected;
        if (cachedResponse != null) {
            collected = collectResult(cachedResponse);
        } else {
            LOGGER.info("Reformulating: {}", identifier);
            String response = llm.generate(request);
            cache.put(key, response);
            collected = collectResult(response);
        }
        
        if (collected.size() != count) {
            LOGGER.warn("Expected {} sentences, but found {} for {} with collected: {}", count, collected.size(), identifier, collected);
        }
        return collected;
    }

    /**
     * Collects the reformulated results from the response.
     * @param response the response of the language model
     * @return the reformulated results from the response
     */
    protected abstract List<String> collectResult(String response);
}
