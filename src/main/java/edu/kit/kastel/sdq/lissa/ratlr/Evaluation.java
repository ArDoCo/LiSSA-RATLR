/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.kastel.sdq.lissa.ratlr.artifactprovider.ArtifactProvider;
import edu.kit.kastel.sdq.lissa.ratlr.cache.CacheManager;
import edu.kit.kastel.sdq.lissa.ratlr.classifier.Classifier;
import edu.kit.kastel.sdq.lissa.ratlr.configuration.Configuration;
import edu.kit.kastel.sdq.lissa.ratlr.elementstore.ElementStore;
import edu.kit.kastel.sdq.lissa.ratlr.embeddingcreator.EmbeddingCreator;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.TraceLink;
import edu.kit.kastel.sdq.lissa.ratlr.postprocessor.TraceLinkIdPostprocessor;
import edu.kit.kastel.sdq.lissa.ratlr.preprocessor.Preprocessor;
import edu.kit.kastel.sdq.lissa.ratlr.resultaggregator.ResultAggregator;

public class Evaluation {

    private static final Logger logger = LoggerFactory.getLogger(Evaluation.class);
    private final Path configFile;

    private Configuration configuration;

    private ArtifactProvider sourceArtifactProvider;
    private ArtifactProvider targetArtifactProvider;
    private Preprocessor sourcePreprocessor;
    private Preprocessor targetPreprocessor;
    private EmbeddingCreator embeddingCreator;
    private ElementStore sourceStore;
    private ElementStore targetStore;
    private Classifier classifier;
    private ResultAggregator aggregator;
    private TraceLinkIdPostprocessor traceLinkIdPostProcessor;

    public Evaluation(Path configFile) throws IOException {
        this.configFile = Objects.requireNonNull(configFile);
        setup();
    }

    private void setup() throws IOException {

        configuration = new ObjectMapper().readValue(configFile.toFile(), Configuration.class);
        CacheManager.setCacheDir(configuration.cacheDir());

        sourceArtifactProvider = ArtifactProvider.createArtifactProvider(configuration.sourceArtifactProvider());
        targetArtifactProvider = ArtifactProvider.createArtifactProvider(configuration.targetArtifactProvider());

        sourcePreprocessor = Preprocessor.createPreprocessor(configuration.sourcePreprocessor());
        targetPreprocessor = Preprocessor.createPreprocessor(configuration.targetPreprocessor());

        embeddingCreator = EmbeddingCreator.createEmbeddingCreator(configuration.embeddingCreator());
        sourceStore = new ElementStore(configuration.sourceStore(), false);
        targetStore = new ElementStore(configuration.targetStore(), true);

        classifier = Classifier.createClassifier(configuration.classifier());
        aggregator = ResultAggregator.createResultAggregator(configuration.resultAggregator());

        traceLinkIdPostProcessor =
                TraceLinkIdPostprocessor.createTraceLinkIdPostprocessor(configuration.traceLinkIdPostprocessor());

        configuration.serializeAndDestroyConfiguration();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Set<TraceLink> run() {

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
                traceLinks, configFile.toFile(), configuration, sourceArtifacts.size(), targetArtifacts.size());
        Statistics.saveTraceLinks(traceLinks, configFile.toFile(), configuration);

        return traceLinks;
    }

    public int getSourceArtifactCount() {
        return sourceArtifactProvider.getArtifacts().size();
    }

    public int getTargetArtifactCount() {
        return targetArtifactProvider.getArtifacts().size();
    }
}
