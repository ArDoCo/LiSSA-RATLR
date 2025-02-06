/* Licensed under MIT 2025. */
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

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.metrics.ClassificationMetricsCalculator;
import edu.kit.kastel.sdq.lissa.ratlr.configuration.Configuration;
import edu.kit.kastel.sdq.lissa.ratlr.configuration.GoldStandardConfiguration;
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
            throws UncheckedIOException {
        generateStatistics(
                configuration.getConfigurationIdentifierForFile(configFile.getName()),
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
            GoldStandardConfiguration goldStandardConfiguration,
            int sourceArtifacts,
            int targetArtifacts)
            throws UncheckedIOException {

        if (goldStandardConfiguration == null || goldStandardConfiguration.path() == null) {
            logger.info(
                    "Skipping statistics generation since no path to ground truth has been provided as first command line argument");
            return;
        }

        Set<TraceLink> validTraceLinks = getTraceLinksFromGoldStandard(goldStandardConfiguration);

        ClassificationMetricsCalculator cmc = ClassificationMetricsCalculator.getInstance();
        var classification = cmc.calculateMetrics(traceLinks, validTraceLinks, null);
        classification.prettyPrint();

        // Store information to one file (config and results)
        var resultFile = new File("results-" + configurationIdentifier + ".md");
        StringBuilder result = new StringBuilder();
        result.append("## Configuration\n```json\n")
                .append(configurationSummary)
                .append("\n```\n\n");
        result.append("## Stats\n");
        result.append("* # TraceLinks (GS): ").append(validTraceLinks.size()).append("\n");
        result.append("* # Source Artifacts: ").append(sourceArtifacts).append("\n");
        result.append("* # Target Artifacts: ").append(targetArtifacts).append("\n");
        result.append("## Results\n");
        result.append("* True Positives: ")
                .append(classification.getTruePositives().size())
                .append("\n");
        result.append("* False Positives: ")
                .append(classification.getFalsePositives().size())
                .append("\n");
        result.append("* False Negatives: ")
                .append(classification.getFalseNegatives().size())
                .append("\n");
        result.append("* Precision: ").append(classification.getPrecision()).append("\n");
        result.append("* Recall: ").append(classification.getRecall()).append("\n");
        result.append("* F1: ").append(classification.getF1()).append("\n");

        logger.info("Storing results to {}", resultFile.getName());
        try {
            Files.writeString(resultFile.toPath(), result.toString(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @NotNull
    private static Set<TraceLink> getTraceLinksFromGoldStandard(GoldStandardConfiguration goldStandardConfiguration) {
        File groundTruth = new File(goldStandardConfiguration.path());
        boolean header = goldStandardConfiguration.hasHeader();
        logger.info("Skipping header: {}", header);
        Set<TraceLink> validTraceLinks;
        try {
            validTraceLinks = Files.readAllLines(groundTruth.toPath()).stream()
                    .skip(header ? 1 : 0)
                    .map(l -> l.split(","))
                    .map(it -> new TraceLink(it[0], it[1]))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validTraceLinks;
    }

    public static void saveTraceLinks(Set<TraceLink> traceLinks, File configFile, Configuration configuration)
            throws UncheckedIOException {
        var fileName = "traceLinks-" + configuration.getConfigurationIdentifierForFile(configFile.getName()) + ".csv";
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
