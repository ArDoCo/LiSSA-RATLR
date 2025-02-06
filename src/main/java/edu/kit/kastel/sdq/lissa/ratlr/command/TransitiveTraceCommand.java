package edu.kit.kastel.sdq.lissa.ratlr.command;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.kastel.sdq.lissa.ratlr.Configuration;
import edu.kit.kastel.sdq.lissa.ratlr.Evaluation;
import edu.kit.kastel.sdq.lissa.ratlr.Statistics;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.TraceLink;
import edu.kit.kastel.sdq.lissa.ratlr.utils.KeyGenerator;
import picocli.CommandLine;

@CommandLine.Command(
        name = "transitive",
        mixinStandardHelpOptions = true,
        description = "Invokes the pipeline (transitive trace link) and evaluates it")
public class TransitiveTraceCommand implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TransitiveTraceCommand.class);

    @CommandLine.Option(
            names = {"-c", "--configs"},
            arity = "2..*",
            description = "Specifies two or more config paths to be invoked sequentially.")
    private Path[] transitiveTraceConfigs;

    @CommandLine.Option(
            names = {"-e", "--evaluation-config"},
            description = "Specifies the evaluation config path to be invoked after the transitive trace link.")
    private Path evaluationConfig;

    @Override
    public void run() {
        if (transitiveTraceConfigs == null || transitiveTraceConfigs.length < 2) {
            logger.error("At least two config paths are required for transitive trace link");
            return;
        }

        if (evaluationConfig == null) {
            logger.warn("No evaluation config path provided, so we just produce the transitive trace links");
        }

        List<Evaluation> evaluations = new ArrayList<>();
        Queue<Set<TraceLink>> traceLinks = new ArrayDeque<>();
        try {
            for (Path traceConfig : transitiveTraceConfigs) {
                logger.info("Invoking the pipeline with '{}'", traceConfig);
                Evaluation evaluation = new Evaluation(traceConfig);
                evaluations.add(evaluation);
                var traceLinksForRun = evaluation.run();
                logger.info("Found {} trace links", traceLinksForRun.size());
                traceLinks.add(traceLinksForRun);
            }
        } catch (IOException e) {
            logger.warn("Configuration threw an exception: {}", e.getMessage());
            return;
        }

        Configuration.GoldStandardConfiguration goldStandardConfiguration = null;
        if (evaluationConfig != null) {
            try {
                goldStandardConfiguration = new ObjectMapper()
                        .readValue(evaluationConfig.toFile(), Configuration.GoldStandardConfiguration.class);
            } catch (IOException e) {
                logger.error("Evaluation config threw an exception: {}", e.getMessage());
                return;
            }
        }

        Set<TraceLink> transitiveTraceLinks = new LinkedHashSet<>(traceLinks.poll());
        while (!traceLinks.isEmpty()) {
            Set<TraceLink> currentLinks = transitiveTraceLinks;
            Set<TraceLink> nextLinks = traceLinks.poll();
            transitiveTraceLinks = new LinkedHashSet<>();
            logger.info("Joining trace links of size {} and {}", currentLinks.size(), nextLinks.size());
            for (TraceLink currentLink : currentLinks) {
                for (TraceLink nextLink : nextLinks) {
                    if (currentLink.targetId().equals(nextLink.sourceId())) {
                        transitiveTraceLinks.add(new TraceLink(currentLink.sourceId(), nextLink.targetId()));
                    }
                }
            }
            logger.info("Found transitive links of size {}", transitiveTraceLinks.size());
        }

        String key = createKey(evaluations, goldStandardConfiguration);
        Statistics.saveTraceLinks(transitiveTraceLinks, "transitive-trace-links_" + key + ".csv");

        if (goldStandardConfiguration != null) {
            int sourceArtifacts = evaluations.getFirst().getSourceArtifactCount();
            int targetArtifacts = evaluations.getLast().getTargetArtifactCount();

            try {
                Statistics.generateStatistics(
                        "transitive-trace-links_" + key,
                        evaluations.stream()
                                        .map(it -> it.getConfiguration().serializeAndDestroyConfiguration())
                                        .reduce("", (a, b) -> a + "\n\n" + b)
                                + "\n\n" + goldStandardConfiguration,
                        transitiveTraceLinks,
                        goldStandardConfiguration,
                        sourceArtifacts,
                        targetArtifacts);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private String createKey(
            List<Evaluation> evaluations, Configuration.GoldStandardConfiguration goldStandardConfiguration) {
        String evaluationKey = evaluations.stream()
                .map(Evaluation::getConfiguration)
                .map(Configuration::serializeAndDestroyConfiguration)
                .reduce("", String::concat);
        String goldStandardKey = goldStandardConfiguration == null ? "" : goldStandardConfiguration.toString();
        return KeyGenerator.generateKey(evaluationKey + goldStandardKey);
    }
}
