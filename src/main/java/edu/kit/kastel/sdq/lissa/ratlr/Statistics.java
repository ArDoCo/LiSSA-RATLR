package edu.kit.kastel.sdq.lissa.ratlr;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.TraceLink;

public final class Statistics {
    private static final Logger logger = LoggerFactory.getLogger(Statistics.class);

    private Statistics() {
        throw new IllegalAccessError("Utility class");
    }

    public static void generateStatistics(
            Set<TraceLink> traceLinks,
            File configFile,
            Configuration configuration,
            int sourceArtifacts,
            int targetArtifacts)
            throws IOException {
        generateStatistics(
                configuration.getConfigurationIdentifierForFile(configFile),
                configuration.serializeAndDestroyConfiguration(),
                traceLinks,
                configuration.goldStandardConfiguration(),
                sourceArtifacts,
                targetArtifacts);
    }

    public static void generateStatistics(
            String configurationIdentifier,
            String configurationSummary,
            Set<TraceLink> traceLinks,
            Configuration.GoldStandardConfiguration goldStandardConfiguration,
            int sourceArtifacts,
            int targetArtifacts)
            throws IOException {
        if (goldStandardConfiguration == null || goldStandardConfiguration.path() == null) {
            logger.info(
                    "Skipping statistics generation since no path to ground truth has been provided as first command line argument");
            return;
        }

        File groundTruth = new File(goldStandardConfiguration.path());
        boolean header = goldStandardConfiguration.hasHeader();
        logger.info("Skipping header: {}", header);
        Set<TraceLink> validTraceLinks = Files.readAllLines(groundTruth.toPath()).stream()
                .skip(header ? 1 : 0)
                .map(l -> l.split(","))
                .map(it -> new TraceLink(it[0], it[1]))
                .collect(Collectors.toSet());

        ClassificationMetricsCalculator cmc = ClassificationMetricsCalculator.getInstance();
        var classification = cmc.calculateMetrics(traceLinks, validTraceLinks, null);
        classification.prettyPrint();

        // Store information to one file (config and results)
        var resultFile = new File("results-" + configurationIdentifier + ".md");
        logger.info("Storing results to {}", resultFile.getName());
        Files.writeString(resultFile.toPath(), "## Configuration\n```json\n" + configurationSummary + "\n```\n\n");
        Files.writeString(resultFile.toPath(), "## Stats\n", StandardOpenOption.APPEND);
        Files.writeString(
                resultFile.toPath(),
                "* # TraceLinks (GS): " + validTraceLinks.size() + "\n",
                StandardOpenOption.APPEND);
        Files.writeString(
                resultFile.toPath(), "* # Source Artifacts: " + sourceArtifacts + "\n", StandardOpenOption.APPEND);
        Files.writeString(
                resultFile.toPath(), "* # Target Artifacts: " + targetArtifacts + "\n", StandardOpenOption.APPEND);
        Files.writeString(resultFile.toPath(), "## Results\n", StandardOpenOption.APPEND);
        Files.writeString(
                resultFile.toPath(),
                "* True Positives: " + classification.getTruePositives().size() + "\n",
                StandardOpenOption.APPEND);
        Files.writeString(
                resultFile.toPath(),
                "* False Positives: " + classification.getFalsePositives().size() + "\n",
                StandardOpenOption.APPEND);
        Files.writeString(
                resultFile.toPath(),
                "* False Negatives: " + classification.getFalseNegatives().size() + "\n",
                StandardOpenOption.APPEND);
        Files.writeString(
                resultFile.toPath(), "* Precision: " + classification.getPrecision() + "\n", StandardOpenOption.APPEND);
        Files.writeString(
                resultFile.toPath(), "* Recall: " + classification.getRecall() + "\n", StandardOpenOption.APPEND);
        Files.writeString(resultFile.toPath(), "* F1: " + classification.getF1() + "\n", StandardOpenOption.APPEND);
    }

    public static void saveTraceLinks(Set<TraceLink> traceLinks, File configFile, Configuration configuration)
            throws IOException {
        var fileName = "traceLinks-" + configuration.getConfigurationIdentifierForFile(configFile) + ".csv";
        saveTraceLinks(traceLinks, fileName);
    }

    public static void saveTraceLinks(Set<TraceLink> traceLinks, String destination) throws UncheckedIOException {
        logger.info("Storing trace links to {}", destination);

        List<TraceLink> orderedTraceLinks = new ArrayList<>(traceLinks);
        orderedTraceLinks.sort(Comparator.comparing(TraceLink::sourceId).thenComparing(TraceLink::targetId));

        String csvResult = orderedTraceLinks.stream()
                .map(it -> it.sourceId() + "," + it.targetId())
                .collect(Collectors.joining("\n"));
        try {
            Files.writeString(new File(destination).toPath(), csvResult, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
