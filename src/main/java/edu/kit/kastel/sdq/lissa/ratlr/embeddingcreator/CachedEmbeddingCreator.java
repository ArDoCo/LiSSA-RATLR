/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.embeddingcreator;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import dev.langchain4j.model.embedding.EmbeddingModel;
import edu.kit.kastel.sdq.lissa.ratlr.cache.Cache;
import edu.kit.kastel.sdq.lissa.ratlr.cache.CacheManager;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;
import edu.kit.kastel.sdq.lissa.ratlr.utils.KeyGenerator;

abstract class CachedEmbeddingCreator extends EmbeddingCreator {
    // TODO Handle Token Length better .. 8192 is the length for ada
    private static final int MAX_TOKEN_LENGTH = 8000;

    private static final Logger STATIC_LOGGER = LoggerFactory.getLogger(CachedEmbeddingCreator.class);
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Cache cache;
    private final EmbeddingModel embeddingModel;
    private final String rawNameOfModel;
    private final int threads;

    protected CachedEmbeddingCreator(String model, int threads, String... params) {
        this.cache = CacheManager.getDefaultInstance()
                .getCache(this.getClass().getSimpleName() + "_" + Objects.requireNonNull(model));
        this.embeddingModel = Objects.requireNonNull(createEmbeddingModel(model, params));
        this.rawNameOfModel = model;
        this.threads = Math.max(1, threads);
    }

    protected abstract EmbeddingModel createEmbeddingModel(String model, String... params);

    @Override
    public final List<float[]> calculateEmbeddings(List<Element> elements) {
        if (threads == 1) return calculateEmbeddingsSequential(elements);

        int threadCount = Math.min(threads, elements.size());
        int numberOfElementsPerThread = elements.size() / threadCount;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<List<float[]>>> futureResults = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            int start = i * numberOfElementsPerThread;
            int end = i == threadCount - 1 ? elements.size() : (i + 1) * numberOfElementsPerThread;
            List<Element> subList = elements.subList(start, end);
            futureResults.add(executor.submit(() -> {
                var embeddingModelInstance = createEmbeddingModel(this.rawNameOfModel);
                return calculateEmbeddingsSequential(embeddingModelInstance, subList);
            }));
        }
        logger.info("Waiting for classification to finish. Elements in queue: {}", futureResults.size());

        try {
            executor.shutdown();
            boolean success = executor.awaitTermination(1, TimeUnit.DAYS);
            if (!success) {
                logger.error("Embedding did not finish in time.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        executor.close();

        return futureResults.stream()
                .map(Future::resultNow)
                .flatMap(Collection::stream)
                .toList();
    }

    private List<float[]> calculateEmbeddingsSequential(List<Element> elements) {
        return this.calculateEmbeddingsSequential(this.embeddingModel, elements);
    }

    private List<float[]> calculateEmbeddingsSequential(EmbeddingModel embeddingModel, List<Element> elements) {
        List<float[]> embeddings = new ArrayList<>();
        for (Element element : elements) {
            embeddings.add(calculateFinalEmbedding(embeddingModel, cache, rawNameOfModel, element));
        }
        return embeddings;
    }

    private static float[] calculateFinalEmbedding(
            EmbeddingModel embeddingModel, Cache cache, String rawNameOfModel, Element element) {
        String key = KeyGenerator.generateKey(element.getContent());
        float[] cachedEmbedding = cache.get(key, float[].class);
        if (cachedEmbedding != null) {
            return cachedEmbedding;
        } else {
            STATIC_LOGGER.info("Calculating embedding for: {}", element.getIdentifier());
            try {
                float[] embedding =
                        embeddingModel.embed(element.getContent()).content().vector();
                cache.put(key, embedding);
                return embedding;
            } catch (Exception e) {
                STATIC_LOGGER.error(
                        "Error while calculating embedding for .. try to fix ..: {}", element.getIdentifier());
                // Probably the length was too long .. check that
                return tryToFixWithLength(embeddingModel, cache, rawNameOfModel, key, element.getContent());
            }
        }
    }

    private static float[] tryToFixWithLength(
            EmbeddingModel embeddingModel, Cache cache, String rawNameOfModel, String key, String content) {
        String newKey = key + "_fixed_" + MAX_TOKEN_LENGTH;
        float[] cachedEmbedding = cache.get(newKey, float[].class);
        if (cachedEmbedding != null) {
            STATIC_LOGGER.info("using fixed embedding for: {}", key);
            return cachedEmbedding;
        }
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        Encoding encoding = registry.getEncodingForModel(rawNameOfModel)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown Embedding Model. Don't know how to handle previous exception"));
        int tokens = encoding.countTokens(content);
        if (tokens < MAX_TOKEN_LENGTH)
            throw new IllegalArgumentException(
                    "Token length was not too long. Don't know how to handle previous exception");

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
        STATIC_LOGGER.info("using fixed embedding for: {}", key);
        cache.put(newKey, embedding);
        return embedding;
    }
}
