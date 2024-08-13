package edu.kit.kastel.sdq.lissa.ratlr.embeddingcreator;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import dev.langchain4j.model.embedding.EmbeddingModel;
import edu.kit.kastel.sdq.lissa.ratlr.cache.Cache;
import edu.kit.kastel.sdq.lissa.ratlr.cache.CacheManager;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

abstract class CachedEmbeddingCreator extends EmbeddingCreator {
    // TODO Handle Token Length better .. 8192 is the length for ada
    private static final int MAX_TOKEN_LENGTH = 8000;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Cache cache;
    private final EmbeddingModel embeddingModel;
    private final String rawNameOfModel;

    protected CachedEmbeddingCreator(String model, String... params) {
        this.cache = CacheManager.getDefaultInstance().getCache(this.getClass().getSimpleName() + "_" + Objects.requireNonNull(model));
        this.embeddingModel = Objects.requireNonNull(createEmbeddingModel(model, params));
        this.rawNameOfModel = model;
    }

    protected abstract EmbeddingModel createEmbeddingModel(String model, String... params);

    @Override
    public final List<float[]> calculateEmbeddings(List<Element> elements) {
        List<float[]> embeddings = new ArrayList<>();
        for (Element element : elements) {
            String key = UUID.nameUUIDFromBytes(element.getContent().getBytes()).toString();
            float[] cachedEmbedding = cache.get(key, float[].class);
            if (cachedEmbedding != null) {
                embeddings.add(cachedEmbedding);
            } else {
                logger.info("Calculating embedding for: {}", element.getIdentifier());
                try {
                    float[] embedding = embeddingModel.embed(element.getContent()).content().vector();
                    cache.put(key, embedding);
                    embeddings.add(embedding);
                } catch (Exception e) {
                    logger.error("Error while calculating embedding for .. try to fix ..: {}", element.getIdentifier());
                    // Probably the length was too long .. check that
                    tryToFixWithLength(key, element.getContent(), embeddings);
                }
            }
        }
        return embeddings;
    }

    private void tryToFixWithLength(String key, String content, List<float[]> embeddings) {
        String newKey = key + "_fixed_" + MAX_TOKEN_LENGTH;
        float[] cachedEmbedding = cache.get(newKey, float[].class);
        if (cachedEmbedding != null) {
            logger.info("using fixed embedding for: {}", key);
            embeddings.add(cachedEmbedding);
            return;
        }
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        Encoding encoding = registry.getEncodingForModel(this.rawNameOfModel)
                .orElseThrow(() -> new IllegalArgumentException("Unknown Embedding Model. Don't know how to handle previous exception"));
        int tokens = encoding.countTokens(content);
        if (tokens < MAX_TOKEN_LENGTH)
            throw new IllegalArgumentException("Token length was not too long. Don't know how to handle previous exception");

        // Binary search for max length of string
        int left = 0;
        int right = content.length();
        while (left < right) {
            int mid = left + (right - left) / 2;
            String subContent = content.substring(0, mid);
            int subTokens = encoding.countTokens(subContent);
            if (subTokens >= MAX_TOKEN_LENGTH) {
                right = mid;
            } else {
                left = mid + 1;
            }
        }
        String fixedContent = content.substring(0, left);
        float[] embedding = embeddingModel.embed(fixedContent).content().vector();
        logger.info("using fixed embedding for: {}", key);
        cache.put(newKey, embedding);
        embeddings.add(embedding);
    }
}
