/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.kastel.sdq.lissa.ratlr.artifactprovider.ArtifactProvider;
import edu.kit.kastel.sdq.lissa.ratlr.cache.CacheManager;
import edu.kit.kastel.sdq.lissa.ratlr.classifier.Classifier;
import edu.kit.kastel.sdq.lissa.ratlr.configuration.Configuration;
import edu.kit.kastel.sdq.lissa.ratlr.elementstore.ElementStore;
import edu.kit.kastel.sdq.lissa.ratlr.embeddingcreator.EmbeddingCreator;
import edu.kit.kastel.sdq.lissa.ratlr.postprocessor.TraceLinkIdPostprocessor;
import edu.kit.kastel.sdq.lissa.ratlr.preprocessor.Preprocessor;
import edu.kit.kastel.sdq.lissa.ratlr.resultaggregator.ResultAggregator;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        var configFilePath = args.length == 0 ? "config.json" : args[0];
        var configFile = new File(configFilePath);
        Configuration configuration = new ObjectMapper().readValue(configFile, Configuration.class);
        CacheManager.setCacheDir(configuration.cacheDir());

        ArtifactProvider sourceArtifactProvider =
                ArtifactProvider.createArtifactProvider(configuration.sourceArtifactProvider());
        ArtifactProvider targetArtifactProvider =
                ArtifactProvider.createArtifactProvider(configuration.targetArtifactProvider());

        Preprocessor sourcePreprocessor = Preprocessor.createPreprocessor(configuration.sourcePreprocessor());
        Preprocessor targetPreprocessor = Preprocessor.createPreprocessor(configuration.targetPreprocessor());

        EmbeddingCreator embeddingCreator = EmbeddingCreator.createEmbeddingCreator(configuration.embeddingCreator());
        ElementStore sourceStore = new ElementStore(configuration.sourceStore(), false);
        ElementStore targetStore = new ElementStore(configuration.targetStore(), true);

        Classifier classifier = Classifier.createClassifier(configuration.classifier());
        ResultAggregator aggregator = ResultAggregator.createResultAggregator(configuration.resultAggregator());

        TraceLinkIdPostprocessor traceLinkIdPostProcessor =
                TraceLinkIdPostprocessor.createTraceLinkIdPostprocessor(configuration.traceLinkIdPostprocessor());

        configuration.serializeAndDestroyConfiguration();

        // RUN
        logger.info("Loading artifacts");
        var sourceArtifacts = sourceArtifactProvider.getArtifacts();
        var targetArtifacts = targetArtifactProvider.getArtifacts();

        logger.info("Preprocessing artifacts");
        var sourceElements = sourcePreprocessor.preprocess(sourceArtifacts);
        var targetElements = targetPreprocessor.preprocess(targetArtifacts);

        logger.info("Calculating embeddings");
        var sourceEmbeddings = embeddingCreator.calculateEmbeddings(sourceElements);
        var targetEmbeddings = embeddingCreator.calculateEmbeddings(targetElements);

        logger.info("Building element stores");
        sourceStore.setup(sourceElements, sourceEmbeddings);
        targetStore.setup(targetElements, targetEmbeddings);

        logger.info("Classifying Tracelinks");
        var llmResults = classifier.classify(sourceStore, targetStore);
        var traceLinks = aggregator.aggregate(sourceElements, targetElements, llmResults);

        logger.info("Postprocessing Tracelinks");
        traceLinks = traceLinkIdPostProcessor.postprocess(traceLinks);

        logger.info("Evaluating Results");
        Statistics.generateStatistics(
                traceLinks, configFile, configuration, sourceArtifacts.size(), targetArtifacts.size());
        Statistics.saveTraceLinks(traceLinks, configFile, configuration);
    }
}
