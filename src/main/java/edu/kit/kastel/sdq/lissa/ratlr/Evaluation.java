package edu.kit.kastel.sdq.lissa.ratlr;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.sdq.lissa.ratlr.artifactprovider.ArtifactProvider;
import edu.kit.kastel.sdq.lissa.ratlr.classifier.Classifier;
import edu.kit.kastel.sdq.lissa.ratlr.elementstore.ElementStore;
import edu.kit.kastel.sdq.lissa.ratlr.embeddingcreator.EmbeddingCreator;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.TraceLink;
import edu.kit.kastel.sdq.lissa.ratlr.postprocessor.TraceLinkIdPostprocessor;
import edu.kit.kastel.sdq.lissa.ratlr.preprocessor.Preprocessor;
import edu.kit.kastel.sdq.lissa.ratlr.resultaggregator.ResultAggregator;
import edu.kit.kastel.sdq.lissa.ratlr.utils.KeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.stream.Collectors;

public class Evaluation {

    private static final Logger logger = LoggerFactory.getLogger(Evaluation.class);
    private final Path config;

    public Evaluation(Path config) {
        this.config = config;
    }

    public void run() throws IOException {
        Configuration configuration = new ObjectMapper().readValue(config.toFile(), Configuration.class);

        ArtifactProvider sourceArtifactProvider = ArtifactProvider.createArtifactProvider(configuration.sourceArtifactProvider());
        ArtifactProvider targetArtifactProvider = ArtifactProvider.createArtifactProvider(configuration.targetArtifactProvider());

        Preprocessor sourcePreprocessor = Preprocessor.createPreprocessor(configuration.sourcePreprocessor());
        Preprocessor targetPreprocessor = Preprocessor.createPreprocessor(configuration.targetPreprocessor());

        EmbeddingCreator embeddingCreator = EmbeddingCreator.createEmbeddingCreator(configuration.embeddingCreator());
        ElementStore sourceStore = new ElementStore(configuration.sourceStore(), false);
        ElementStore targetStore = new ElementStore(configuration.targetStore(), true);

        Classifier classifier = Classifier.createClassifier(configuration.classifier());
        ResultAggregator aggregator = ResultAggregator.createResultAggregator(configuration.resultAggregator());

        TraceLinkIdPostprocessor traceLinkIdPostProcessor = TraceLinkIdPostprocessor.createTraceLinkIdPostprocessor(configuration.traceLinkIdPostprocessor());

        configuration.serializeAndDestroyConfiguration();

        if (configuration.goldStandardConfiguration() == null)
            throw new IllegalArgumentException("Gold standard configuration is missing");

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
        generateStatistics(traceLinks, configuration);
    }

    private void generateStatistics(Set<TraceLink> traceLinks, Configuration configuration) throws IOException {
        logger.info("Skipping header: {}", configuration.goldStandardConfiguration().hasHeader());
        Set<TraceLink> validTraceLinks = Files.readAllLines(Path.of(configuration.goldStandardConfiguration().path()))
                .stream()
                .skip(configuration.goldStandardConfiguration().hasHeader() ? 1 : 0)
                .map(l -> l.split(","))
                .map(it -> new TraceLink(it[0], it[1]))
                .collect(Collectors.toSet());

        ClassificationMetricsCalculator cmc = ClassificationMetricsCalculator.getInstance();
        var classification = cmc.calculateMetrics(traceLinks, validTraceLinks, null);
        classification.prettyPrint();

        // Store information to one file (config and results)
        var resultFile = new File("results-" + configuration.traceLinkIdPostprocessor().name() + "-" + KeyGenerator.generateKey(configuration
                .toString()) + ".md");
        logger.info("Storing results to " + resultFile.getName());
        Files.writeString(resultFile.toPath(), "## Configuration\n```json\n" + configuration.serializeAndDestroyConfiguration() + "\n```\n\n");
        Files.writeString(resultFile.toPath(), "## Results\n", StandardOpenOption.APPEND);
        Files.writeString(resultFile.toPath(), "* True Positives: " + classification.getTp().size() + "\n", StandardOpenOption.APPEND);
        Files.writeString(resultFile.toPath(), "* False Positives: " + classification.getFp().size() + "\n", StandardOpenOption.APPEND);
        Files.writeString(resultFile.toPath(), "* False Negatives: " + classification.getFn().size() + "\n", StandardOpenOption.APPEND);
        Files.writeString(resultFile.toPath(), "* Precision: " + classification.getPrecision() + "\n", StandardOpenOption.APPEND);
        Files.writeString(resultFile.toPath(), "* Recall: " + classification.getRecall() + "\n", StandardOpenOption.APPEND);
        Files.writeString(resultFile.toPath(), "* F1: " + classification.getF1() + "\n", StandardOpenOption.APPEND);
    }
}
