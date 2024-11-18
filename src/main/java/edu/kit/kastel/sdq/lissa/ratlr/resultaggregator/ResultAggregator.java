package edu.kit.kastel.sdq.lissa.ratlr.resultaggregator;

import java.util.List;
import java.util.Set;

import edu.kit.kastel.sdq.lissa.ratlr.Configuration;
import edu.kit.kastel.sdq.lissa.ratlr.classifier.ClassificationResult;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.TraceLink;

public abstract class ResultAggregator {
    public abstract Set<TraceLink> aggregate(
            List<Element> sourceElements,
            List<Element> targetElements,
            List<ClassificationResult> classificationResults);

    public static ResultAggregator createResultAggregator(Configuration.ModuleConfiguration configuration) {
        return switch (configuration.name()) {
            case "any_connection" -> new AnyResultAggregator(configuration);
            case "majority" -> new MajorityResultAggregator(configuration);
            default -> throw new IllegalStateException("Unexpected value: " + configuration.name());
        };
    }
}
