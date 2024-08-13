package edu.kit.kastel.sdq.lissa.ratlr.embeddingcreator;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.OnnxEmbeddingModel;
import dev.langchain4j.model.embedding.PoolingMode;
import edu.kit.kastel.sdq.lissa.ratlr.Configuration;

import java.io.File;

public class OnnxEmbeddingCreator extends CachedEmbeddingCreator {
    public OnnxEmbeddingCreator(String model, String pathToModel, String pathToTokenizer) {
        super(model, pathToModel, pathToTokenizer);
    }

    public OnnxEmbeddingCreator(Configuration.ModuleConfiguration configuration) {
        this(configuration.argumentAsString("model"), configuration.argumentAsString("path_to_model"), configuration.argumentAsString("path_to_tokenizer"));
    }

    @Override
    protected EmbeddingModel createEmbeddingModel(String model, String... params) {
        String modelPath = params[0];
        String tokenizerPath = params[1];

        File modelFile = new File(modelPath);
        File tokenizerFile = new File(tokenizerPath);
        if (!modelFile.exists() || !tokenizerFile.exists()) {
            throw new IllegalStateException("Model or Tokenizer file does not exist");
        }

        PoolingMode poolingMode = PoolingMode.MEAN;
        EmbeddingModel embeddingModel = new OnnxEmbeddingModel(modelFile.toPath(), tokenizerFile.toPath(), poolingMode);
        logger.info("Created OnnxEmbeddingModel with model: {} and tokenizer: {}", modelPath, tokenizerPath);
        return embeddingModel;
    }
}
