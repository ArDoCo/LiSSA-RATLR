/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.classifier;

/**
 * Enum representing supported chat language model platforms.
 * Each platform specifies the number of threads to use for parallel execution.
 *
 * <ul>
 *   <li>OPENAI: OpenAI platform (100 threads)</li>
 *   <li>OLLAMA: Ollama platform (1 thread)</li>
 *   <li>BLABLADOR: Blablador platform (100 threads)</li>
 *   <li>DEEPSEEK: DeepSeek platform (1 thread)</li>
 * </ul>
 *
 * @see ChatLanguageModelProvider
 */
public enum ChatLanguageModelPlatform {
    /**
     * OpenAI platform (100 threads).
     */
    OPENAI(100, "gpt-4o-mini"),
    /**
     * Ollama platform (1 thread).
     */
    OLLAMA(1, "llama3:8b"),
    /**
     * Blablador platform (100 threads).
     */
    BLABLADOR(100, "2 - Llama 3.3 70B instruct"),
    /**
     * DeepSeek platform (1 thread).
     */
    DEEPSEEK(1, "deepseek-coder");

    private final int threads;
    private final String defaultModel;

    ChatLanguageModelPlatform(int threads, String defaultModel) {
        this.threads = threads;
        this.defaultModel = defaultModel;
    }

    /**
     * Returns the number of threads for this platform.
     *
     * @return the thread count
     */
    public int getThreads() {
        return threads;
    }

    /**
     * Returns the default model name for this platform.
     *
     * @return the default model name
     */
    public String getDefaultModel() {
        return defaultModel;
    }

    /**
     * Returns the enum value for the given platform name (case-insensitive).
     *
     * @param name the platform name (e.g., "openai")
     * @return the corresponding enum value
     * @throws IllegalArgumentException if the name does not match any platform
     */
    public static ChatLanguageModelPlatform fromString(String name) {
        for (ChatLanguageModelPlatform p : values()) {
            if (p.name().equalsIgnoreCase(name)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Unknown platform: " + name);
    }
}
