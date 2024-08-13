package edu.kit.kastel.sdq.lissa.ratlr.classifier;

import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;

import java.util.List;

public class MockClassifier extends Classifier {
    @Override
    protected List<ClassificationResult> classify(Element source, List<Element> targets) {
        return targets.stream().map(target -> ClassificationResult.of(source, target)).toList();
    }

    @Override
    protected Classifier copyOf() {
        return new MockClassifier();
    }
}
