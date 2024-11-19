package edu.kit.kastel.sdq.lissa.ratlr.resultaggregator;

import edu.kit.kastel.sdq.lissa.ratlr.Configuration;
import edu.kit.kastel.sdq.lissa.ratlr.classifier.ClassificationResult;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.TraceLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MajorityResultAggregator extends GranularityAggregator {

    private static final Logger log = LoggerFactory.getLogger(MajorityResultAggregator.class);
    private final double threshold;
    private final int baseSourceGranularity;
    private final int baseTargetGranularity;

    protected MajorityResultAggregator(Configuration.ModuleConfiguration configuration) {
        super(configuration);
        this.threshold = configuration.argumentAsDouble("threshold", 0.5);
        this.baseSourceGranularity = configuration.argumentAsInt("base_source_granularity", 0);
        this.baseTargetGranularity = configuration.argumentAsInt("base_target_granularity", 0);
    }

    @Override
    public Set<TraceLink> aggregate(
            List<Element> sourceElements,
            List<Element> targetElements,
            List<ClassificationResult> classificationResults) {

        Map<Element, List<Element>> sourcesByTarget = new HashMap<>();
        Map<Element, List<Element>> targetsBySource = new HashMap<>();
        Map<Element, Map<Element, List<Element>>> actualSourcesBySourceBaseByTargetBase = new HashMap<>();
        for (ClassificationResult result : classificationResults) {
            Element sourceBase = buildListOfValidElements(result.source(), baseSourceGranularity, sourceElements).getFirst();
            Element targetBase = buildListOfValidElements(result.target(), baseTargetGranularity, targetElements).getFirst();
            actualSourcesBySourceBaseByTargetBase.putIfAbsent(targetBase, new HashMap<>());
            actualSourcesBySourceBaseByTargetBase.get(targetBase).putIfAbsent(sourceBase, new LinkedList<>());
            actualSourcesBySourceBaseByTargetBase.get(targetBase).get(sourceBase).add(result.source());

            sourcesByTarget.putIfAbsent(result.target(), new LinkedList<>());
            sourcesByTarget.get(result.target()).add(result.source());
            
            targetsBySource.putIfAbsent(result.source(), new LinkedList<>());
            targetsBySource.get(result.source()).add(result.target());
        }
        Set<TraceLink> traceLinks = new LinkedHashSet<>();
        for (Map.Entry<Element, Map<Element, List<Element>>> targetEntry : actualSourcesBySourceBaseByTargetBase.entrySet()) {
            for (Map.Entry<Element, List<Element>> sourceEntry : targetEntry.getValue().entrySet()) {
                int maxLinks = sameSourceElements(sourceEntry.getKey(), sourceElements).size();
                int actualLinks = sourceEntry.getValue().size();
                log.info("{} of {} for source {} and target {}", actualLinks, maxLinks, sourceEntry.getKey().getIdentifier(), targetEntry.getKey().getIdentifier());
                if ((double) actualLinks / maxLinks > threshold) {
                    traceLinks.add(new TraceLink(sourceEntry.getKey().getIdentifier(), targetEntry.getKey().getIdentifier()));
                }
            }
        }
        return traceLinks;
    }
}
