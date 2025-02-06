/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.classifier;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import edu.kit.kastel.sdq.lissa.ratlr.configuration.ModuleConfiguration;
import edu.kit.kastel.sdq.lissa.ratlr.utils.Environment;
import okhttp3.Credentials;

public class ChatLanguageModelProvider {
    public static final String OPENAI = "openai";
    public static final String OLLAMA = "ollama";

    public static final int DEFAULT_SEED = 133742243;

    private final String platform;
    private String model;
    private int seed;

    public ChatLanguageModelProvider(ModuleConfiguration configuration) {
        String[] modeXplatform = configuration.name().split(Classifier.CONFIG_NAME_SEPARATOR, 2);
        if (modeXplatform.length == 1) {
            this.platform = null;
            return;
        }
        this.platform = modeXplatform[1];
        initModelPlatform(configuration);
    }

    public ChatLanguageModel createChatModel() {
        return switch (platform) {
            case OPENAI -> createOpenAiChatModel(model, seed);
            case OLLAMA -> createOllamaChatModel(model, seed);
            default -> throw new IllegalArgumentException("Unsupported platform: " + platform);
        };
    }

    private void initModelPlatform(ModuleConfiguration configuration) {
        this.model = switch (platform) {
            case OPENAI -> configuration.argumentAsString("model", "gpt-4o-mini");
            case OLLAMA -> configuration.argumentAsString("model", "llama3:8b");
            default -> throw new IllegalArgumentException("Unsupported platform: " + platform);
        };
        this.seed = configuration.argumentAsInt("seed", DEFAULT_SEED);
    }

    public String modelName() {
        return Objects.requireNonNull(model, "Model not initialized");
    }

    public static boolean supportsThreads(ModuleConfiguration configuration) {
        return configuration.name().contains(OPENAI);
    }

    public boolean supportsThreads() {
        return platform.equals(OPENAI);
    }

    private static OllamaChatModel createOllamaChatModel(String model, int seed) {
        String host = Environment.getenv("OLLAMA_HOST");
        String user = Environment.getenv("OLLAMA_USER");
        String password = Environment.getenv("OLLAMA_PASSWORD");

        var ollama = OllamaChatModel.builder()
                .baseUrl(host)
                .modelName(model)
                .timeout(Duration.ofMinutes(15))
                .temperature(0.0)
                .seed(seed);
        if (user != null && password != null && !user.isEmpty() && !password.isEmpty()) {
            ollama.customHeaders(Map.of("Authorization", Credentials.basic(user, password)));
        }
        return ollama.build();
    }

    private static OpenAiChatModel createOpenAiChatModel(String model, int seed) {
        String openAiOrganizationId = Environment.getenv("OPENAI_ORGANIZATION_ID");
        String openAiApiKey = Environment.getenv("OPENAI_API_KEY");
        if (openAiOrganizationId == null || openAiApiKey == null) {
            throw new IllegalStateException("OPENAI_ORGANIZATION_ID or OPENAI_API_KEY environment variable not set");
        }
        return new OpenAiChatModel.OpenAiChatModelBuilder()
                .modelName(model)
                .organizationId(openAiOrganizationId)
                .apiKey(openAiApiKey)
                .temperature(0.0)
                .seed(seed)
                .build();
    }
}
