/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.embeddingcreator;

import java.util.List;

import edu.kit.kastel.sdq.lissa.ratlr.configuration.ModuleConfiguration;
import edu.kit.kastel.sdq.lissa.ratlr.context.ContextStore;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;

/**
 * Abstract base class for creating vector embeddings of elements in the LiSSA framework.
 * This class provides the interface for different embedding creation strategies,
 * which convert text elements into vector representations for similarity matching.
 * <p>
 * Embedding creators can access shared context via a {@link edu.kit.kastel.sdq.lissa.ratlr.context.ContextStore},
 * which is passed to their factory method.
 * </p>
 * The framework supports multiple embedding creation backends:
 * <ul>
 *     <li>Ollama: Local embedding generation using Ollama models
 *         <ul>
 *             <li>Requires OLLAMA_EMBEDDING_HOST environment variable</li>
 *             <li>Optional authentication via OLLAMA_EMBEDDING_USER and OLLAMA_EMBEDDING_PASSWORD</li>
 *             <li>Default model: nomic-embed-text:v1.5</li>
 *         </ul>
 *     </li>
 *     <li>OpenAI: Cloud-based embedding generation using OpenAI's API
 *         <ul>
 *             <li>Requires OPENAI_ORGANIZATION_ID and OPENAI_API_KEY environment variables</li>
 *             <li>Default model: text-embedding-ada-002</li>
 *             <li>Supports high-throughput with 40 parallel threads</li>
 *         </ul>
 *     </li>
 *     <li>ONNX: Local embedding generation using ONNX models
 *         <ul>
 *             <li>Requires local model and tokenizer files</li>
 *             <li>Uses mean pooling for embedding generation</li>
 *             <li>Configuration via path_to_model and path_to_tokenizer parameters</li>
 *         </ul>
 *     </li>
 *     <li>Mock: Testing implementation
 *         <ul>
 *             <li>Returns zero vectors for all elements</li>
 *             <li>Useful for testing without actual embedding generation</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * All implementations (except Mock) support caching of embeddings through the
 * {@link CachedEmbeddingCreator} base class, which provides:
 * <ul>
 *     <li>Automatic caching of generated embeddings</li>
 *     <li>Handling of long texts through token length management</li>
 *     <li>Multi-threaded embedding generation where supported</li>
 *     <li>Fallback mechanisms for failed embedding generation</li>
 * </ul>
 */
public abstract class EmbeddingCreator {
    /**
     * Calculates the embedding for a single element.
     * This is a convenience method that delegates to {@link #calculateEmbeddings(List)}.
     *
     * @param element The element to create an embedding for
     * @return The vector embedding of the element
     */
    public float[] calculateEmbedding(Element element) {
        return calculateEmbeddings(List.of(element)).getFirst();
    }

    /**
     * Calculates embeddings for a list of elements.
     * This method must be implemented by concrete embedding creators to provide
     * the actual embedding generation logic.
     *
     * @param elements The list of elements to create embeddings for
     * @return A list of vector embeddings, in the same order as the input elements
     */
    public abstract List<float[]> calculateEmbeddings(List<Element> elements);

    /**
     * Creates an appropriate embedding creator based on the provided configuration.
     * The type of creator is determined by the configuration's name field.
     *
     * @param configuration The configuration specifying which embedding creator to use
     * @param contextStore The shared context store for pipeline components
     * @return An instance of the appropriate embedding creator
     * @throws IllegalStateException If the configuration specifies an unknown creator type
     */
    public static EmbeddingCreator createEmbeddingCreator(
            ModuleConfiguration configuration, ContextStore contextStore) {
        return switch (configuration.name()) {
            case "ollama" -> new OllamaEmbeddingCreator(configuration);
            case "openai" -> new OpenAiEmbeddingCreator(configuration);
            case "onnx" -> new OnnxEmbeddingCreator(configuration);
            case "mock" -> new MockEmbeddingCreator();
            default -> throw new IllegalStateException("Unexpected value: " + configuration.name());
        };
    }
}
