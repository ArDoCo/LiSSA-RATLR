/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.resultaggregator;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.kit.kastel.sdq.lissa.ratlr.classifier.ClassificationResult;
import edu.kit.kastel.sdq.lissa.ratlr.configuration.ModuleConfiguration;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.TraceLink;

public class AnyResultAggregator extends ResultAggregator {
    private final int sourceGranularity;
    private final int targetGranularity;

    public AnyResultAggregator(ModuleConfiguration configuration) {
        this.sourceGranularity = configuration.argumentAsInt("source_granularity", 0);
        this.targetGranularity = configuration.argumentAsInt("target_granularity", 0);
    }

    @Override
    public Set<TraceLink> aggregate(
            List<Element> sourceElements,
            List<Element> targetElements,
            List<ClassificationResult> classificationResults) {
        Set<TraceLink> traceLinks = new LinkedHashSet<>();
        for (var result : classificationResults) {
            var sourceElementsForTraceLink =
                    buildListOfValidElements(result.source(), sourceGranularity, sourceElements);
            var target = result.target();
            var targetElementsForTraceLink = buildListOfValidElements(target, targetGranularity, targetElements);
            for (var sourceElement : sourceElementsForTraceLink) {
                for (var targetElement : targetElementsForTraceLink) {
                    traceLinks.add(new TraceLink(sourceElement.getIdentifier(), targetElement.getIdentifier()));
                }
            }
        }
        return traceLinks;
    }

    private static List<Element> buildListOfValidElements(
            Element element, int desiredGranularity, List<Element> allElements) {
        if (element.getGranularity() == desiredGranularity) {
            return List.of(element);
        }

        if (element.getGranularity() < desiredGranularity) {
            // Element is more course grained than the desired granularity -> find all children that are on the desired
            // granularity
            List<Element> possibleChildren = allElements.stream()
                    .filter(it -> it.getGranularity() == desiredGranularity)
                    .toList();
            // Filter all children that are not transitive children of the element
            return possibleChildren.stream()
                    .filter(it -> isTransitiveChildOf(it, element))
                    .toList();
        }

        // Element is more fine-grained than the desired granularity -> find all parents that are on the desired
        // granularity
        List<Element> possibleParents = allElements.stream()
                .filter(it -> it.getGranularity() == desiredGranularity)
                .toList();
        // Filter all parents that are not transitive parents of the element
        List<Element> validParents = possibleParents.stream()
                .filter(it -> isTransitiveChildOf(element, it))
                .toList();
        assert validParents.size() <= 1;
        return validParents;
    }

    private static boolean isTransitiveChildOf(Element possibleChild, Element parent) {
        Element currentElement = possibleChild;
        while (currentElement != null) {
            if (parent == currentElement) {
                return true;
            }
            currentElement = currentElement.getParent();
        }
        return false;
    }
}
