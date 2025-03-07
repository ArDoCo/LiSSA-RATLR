/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.embeddingcreator;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import edu.kit.kastel.sdq.lissa.ratlr.configuration.ModuleConfiguration;
import edu.kit.kastel.sdq.lissa.ratlr.utils.Environment;

public class OpenAiEmbeddingCreator extends CachedEmbeddingCreator {
    private static final int THREADS = 40;

    public OpenAiEmbeddingCreator(ModuleConfiguration configuration) {
        super(configuration.argumentAsString("model", "text-embedding-ada-002"), THREADS);
    }

    @Override
    protected EmbeddingModel createEmbeddingModel(String model, String... params) {
        String openAiOrganizationId = Environment.getenv("OPENAI_ORGANIZATION_ID");
        String openAiApiKey = Environment.getenv("OPENAI_API_KEY");
        if (openAiOrganizationId == null || openAiApiKey == null) {
            throw new IllegalStateException("OPENAI_ORGANIZATION_ID or OPENAI_API_KEY environment variable not set");
        }
        return new OpenAiEmbeddingModel.OpenAiEmbeddingModelBuilder()
                .modelName(model)
                .organizationId(openAiOrganizationId)
                .apiKey(openAiApiKey)
                .maxRetries(0)
                .build();
    }
}
