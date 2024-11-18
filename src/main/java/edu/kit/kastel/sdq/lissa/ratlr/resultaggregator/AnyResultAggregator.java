package edu.kit.kastel.sdq.lissa.ratlr.resultaggregator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.kit.kastel.sdq.lissa.ratlr.Configuration;
import edu.kit.kastel.sdq.lissa.ratlr.classifier.ClassificationResult;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.TraceLink;

public class AnyResultAggregator extends GranularityAggregator {

    protected AnyResultAggregator(Configuration.ModuleConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Set<TraceLink> aggregate(
            List<Element> sourceElements,
            List<Element> targetElements,
            List<ClassificationResult> classificationResults) {
        Set<TraceLink> traceLinks = new LinkedHashSet<>();
        for (var result : classificationResults) {
            var sourceElementsForTraceLink = sameSourceElements(result.source(), sourceElements);
            var targetElementsForTraceLink = sameTargetElements(result.target(), targetElements);
            for (var sourceElement : sourceElementsForTraceLink) {
                for (var targetElement : targetElementsForTraceLink) {
                    traceLinks.add(new TraceLink(sourceElement.getIdentifier(), targetElement.getIdentifier()));
                }
            }
        }
        return traceLinks;
    }
}
