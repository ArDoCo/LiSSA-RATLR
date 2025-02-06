/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.resultaggregator;

import java.util.List;
import java.util.Set;

import edu.kit.kastel.sdq.lissa.ratlr.classifier.ClassificationResult;
import edu.kit.kastel.sdq.lissa.ratlr.configuration.ModuleConfiguration;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.TraceLink;

public abstract class ResultAggregator {
    public abstract Set<TraceLink> aggregate(
            List<Element> sourceElements,
            List<Element> targetElements,
            List<ClassificationResult> classificationResults);

    public static ResultAggregator createResultAggregator(ModuleConfiguration configuration) {
        return switch (configuration.name()) {
            case "any_connection" -> new AnyResultAggregator(configuration);
            default -> throw new IllegalStateException("Unexpected value: " + configuration.name());
        };
    }
}
