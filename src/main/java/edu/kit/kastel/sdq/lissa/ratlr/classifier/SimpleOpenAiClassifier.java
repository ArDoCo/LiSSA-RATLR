package edu.kit.kastel.sdq.lissa.ratlr.classifier;

import dev.langchain4j.model.chat.ChatLanguageModel;
import edu.kit.kastel.sdq.lissa.ratlr.RatlrConfiguration;

public class SimpleOpenAiClassifier extends SimpleClassifier {
    public SimpleOpenAiClassifier(RatlrConfiguration.ModuleConfiguration configuration) {
        super(configuration.arguments().getOrDefault("model", "gpt-3.5-turbo-0125"));
    }

    @Override
    protected ChatLanguageModel createChatModel(String model) {
        return ChatModels.createOpenAiChatModel(model);
    }
}
