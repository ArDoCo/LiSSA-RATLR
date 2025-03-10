/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.cli.command;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.sdq.lissa.ratlr.Evaluation;
import edu.kit.kastel.sdq.lissa.ratlr.Statistics;
import edu.kit.kastel.sdq.lissa.ratlr.configuration.Configuration;
import edu.kit.kastel.sdq.lissa.ratlr.configuration.GoldStandardConfiguration;
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

        GoldStandardConfiguration goldStandardConfiguration = GoldStandardConfiguration.load(evaluationConfig);

        if (evaluationConfig == null) {
            logger.warn("No evaluation config path provided, so we just produce the transitive trace links");
        } else if (goldStandardConfiguration == null) {
            logger.error("Loading evaluation config was not possible");
            return;
        }

        List<Evaluation> evaluations = new ArrayList<>();
        Queue<Set<TraceLink>> traceLinks = new ArrayDeque<>();
        createNontransitiveTraceLinks(evaluations, traceLinks);

        if (evaluations.size() != traceLinks.size()) {
            logger.error("Number of evaluations and trace link sets do not match");
            return;
        }

        Set<TraceLink> transitiveTraceLinks = calculateTransitiveTraceLinks(traceLinks);

        String key = createKey(evaluations, goldStandardConfiguration);
        Statistics.saveTraceLinks(transitiveTraceLinks, "transitive-trace-links_" + key + ".csv");

        if (goldStandardConfiguration != null) {
            int sourceArtifacts = evaluations.getFirst().getSourceArtifactCount();
            int targetArtifacts = evaluations.getLast().getTargetArtifactCount();
            Statistics.generateStatistics(
                    "transitive-trace-links_" + key,
                    joinConfigs(evaluations, goldStandardConfiguration),
                    transitiveTraceLinks,
                    goldStandardConfiguration,
                    sourceArtifacts,
                    targetArtifacts);
        }
    }

    private Set<TraceLink> calculateTransitiveTraceLinks(Queue<Set<TraceLink>> traceLinks) {
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
        return transitiveTraceLinks;
    }

    private void createNontransitiveTraceLinks(List<Evaluation> evaluations, Queue<Set<TraceLink>> traceLinks) {
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
        }
    }

    private String joinConfigs(List<Evaluation> evaluations, GoldStandardConfiguration goldStandardConfiguration) {
        List<String> evaluationConfigs = evaluations.stream()
                .map(Evaluation::getConfiguration)
                .map(Configuration::serializeAndDestroyConfiguration)
                .collect(Collectors.toCollection(ArrayList::new));

        if (goldStandardConfiguration != null) {
            evaluationConfigs.add(goldStandardConfiguration.toString());
        }

        return String.join("\n", evaluationConfigs);
    }

    private String createKey(List<Evaluation> evaluations, GoldStandardConfiguration goldStandardConfiguration) {
        return KeyGenerator.generateKey(joinConfigs(evaluations, goldStandardConfiguration));
    }
}
