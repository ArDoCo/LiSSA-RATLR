package edu.kit.kastel.sdq.lissa.ratlr.resultaggregator;

import edu.kit.kastel.sdq.lissa.ratlr.configuration.ModuleConfiguration;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;

import java.util.List;

public abstract class GranularityAggregator extends ResultAggregator {

    private final int sourceGranularity;
    private final int targetGranularity;

    protected GranularityAggregator(ModuleConfiguration configuration) {
        this.sourceGranularity = configuration.argumentAsInt("source_granularity", 0);
        this.targetGranularity = configuration.argumentAsInt("target_granularity", 0);
    }

    protected List<Element> sameSourceElements(Element element, List<Element> allElements) {
        return buildListOfValidElements(element, sourceGranularity, allElements);
    }

    protected List<Element> sameTargetElements(Element element, List<Element> allElements) {
        return buildListOfValidElements(element, targetGranularity, allElements);
    }

    public static List<Element> buildListOfValidElements(
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
                    .filter(it -> it.isTransitiveChildOf(element))
                    .toList();
        }

        // Element is more fine-grained than the desired granularity -> find all parents that are on the desired
        // granularity
        List<Element> possibleParents = allElements.stream()
                .filter(it -> it.getGranularity() == desiredGranularity)
                .toList();
        // Filter all parents that are not transitive parents of the element
        List<Element> validParents = possibleParents.stream()
                .filter(element::isTransitiveChildOf)
                .toList();
        assert validParents.size() <= 1;
        return validParents;
    }
}
