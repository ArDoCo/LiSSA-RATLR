package edu.kit.kastel.sdq.lissa.ratlr.resultaggregator;

import edu.kit.kastel.sdq.lissa.ratlr.Configuration;
import edu.kit.kastel.sdq.lissa.ratlr.classifier.ClassificationResult;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.TraceLink;
import edu.kit.kastel.sdq.lissa.ratlr.utils.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MajorityResultAggregator extends GranularityAggregator {

    private final double threshold;

    protected MajorityResultAggregator(Configuration.ModuleConfiguration configuration) {
        super(configuration);
        this.threshold = configuration.argumentAsDouble("threshold", 0.5);
    }

    @Override
    public Set<TraceLink> aggregate(
            List<Element> sourceElements,
            List<Element> targetElements,
            List<ClassificationResult> classificationResults) {

        Map<Pair<String, String>, SourceTargetRegistration> registeredElementsByTopIdentifier
                = registerResults(sourceElements, targetElements, classificationResults);

        Set<TraceLink> traceLinks = new LinkedHashSet<>();
        registeredElementsByTopIdentifier.forEach((key, value) -> {
            int maxLinks = value.source().sameGranularityElements.size() + value.target().sameGranularityElements.size();
            int actualLinks = value.source().registeredElements.size() + value.target().registeredElements.size();
            if ((double) actualLinks / maxLinks > threshold) {
                traceLinks.add(new TraceLink(value.source().sameGranularityElements.getFirst().getParent().getIdentifier()
                        , value.target().sameGranularityElements.getFirst().getIdentifier()));
            }
        });
        return traceLinks;
    }

    private Map<Pair<String, String>, SourceTargetRegistration> registerResults(List<Element> sourceElements, List<Element> targetElements, List<ClassificationResult> classificationResults) {
        Map<Pair<String, String>, SourceTargetRegistration> registeredElementsByTopIdentifier = new HashMap<>();
        for (var result : classificationResults) {
            String topSource = result.source().getTopParent().getIdentifier();
            String topTarget = result.target().getTopParent().getIdentifier();
            Pair<String, String> key = new Pair<>(topSource, topTarget);

            registeredElementsByTopIdentifier.computeIfAbsent(key
                    , ignored -> new SourceTargetRegistration(new ElementRegistration(sameSourceElements(result.source(), sourceElements))
                            , new ElementRegistration(sameTargetElements(result.target(), targetElements))));
            registeredElementsByTopIdentifier.get(key).source().registeredElements.add(result.source());
            registeredElementsByTopIdentifier.get(key).target().registeredElements.add(result.target());
        }
        return registeredElementsByTopIdentifier;
    }

    private static final class ElementRegistration {
        private final List<Element> sameGranularityElements;
        private final List<Element> registeredElements;

        public ElementRegistration(List<Element> sameGranularityElements) {
            this.sameGranularityElements = Collections.unmodifiableList(sameGranularityElements);
            this.registeredElements = new LinkedList<>();
        }
    }

    private record SourceTargetRegistration(ElementRegistration source, ElementRegistration target) {
    }
}
