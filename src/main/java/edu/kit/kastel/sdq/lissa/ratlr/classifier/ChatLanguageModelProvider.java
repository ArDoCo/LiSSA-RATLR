package edu.kit.kastel.sdq.lissa.ratlr.classifier;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import edu.kit.kastel.sdq.lissa.ratlr.Configuration;
import edu.kit.kastel.sdq.lissa.ratlr.Environment;
import okhttp3.Credentials;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public class ChatLanguageModelProvider {
    public static final String OPENAI = "openai";
    public static final String OLLAMA = "ollama";

    private final String platform;
    private String model;

    public ChatLanguageModelProvider(Configuration.ModuleConfiguration configuration) {
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
            case OPENAI -> createOpenAiChatModel(model);
            case OLLAMA -> createOllamaChatModel(model);
            default -> throw new IllegalArgumentException("Unsupported platform: " + platform);
        };
    }

    private void initModelPlatform(Configuration.ModuleConfiguration configuration) {
        this.model = switch (platform) {
            case OPENAI -> configuration.argumentAsString("model", "gpt-4o-mini");
            case OLLAMA -> configuration.argumentAsString("model", "llama3:8b");
            default -> throw new IllegalArgumentException("Unsupported platform: " + platform);
        };
    }

    public String modelName() {
        return Objects.requireNonNull(model, "Model not initialized");
    }

    private static OllamaChatModel createOllamaChatModel(String model) {
        String host = Environment.getenv("OLLAMA_HOST");
        String user = Environment.getenv("OLLAMA_USER");
        String password = Environment.getenv("OLLAMA_PASSWORD");

        var ollama = OllamaChatModel.builder().baseUrl(host).modelName(model).timeout(Duration.ofMinutes(5)).temperature(0.0);
        if (user != null && password != null && !user.isEmpty() && !password.isEmpty()) {
            ollama.customHeaders(Map.of("Authorization", Credentials.basic(user, password)));
        }
        return ollama.build();
    }

    private static OpenAiChatModel createOpenAiChatModel(String model) {
        String openAiOrganizationId = Environment.getenv("OPENAI_ORGANIZATION_ID");
        String openAiApiKey = Environment.getenv("OPENAI_API_KEY");
        if (openAiOrganizationId == null || openAiApiKey == null) {
            throw new IllegalStateException("OPENAI_ORGANIZATION_ID or OPENAI_API_KEY environment variable not set");
        }
        return new OpenAiChatModel.OpenAiChatModelBuilder().modelName(model).organizationId(openAiOrganizationId).apiKey(openAiApiKey).temperature(0.0).build();
    }
}
