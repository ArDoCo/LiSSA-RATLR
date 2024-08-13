package edu.kit.kastel.sdq.lissa.ratlr.classifier;

import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;

public record ClassificationResult(Element source, Element target, double confidence) {
    public ClassificationResult {
        if (confidence < 0 || confidence > 1) {
            throw new IllegalArgumentException("Confidence must be between 0 and 1");
        }
    }

    public static ClassificationResult of(Element source, Element target) {
        return new ClassificationResult(source, target, 1.0);
    }

    public static ClassificationResult of(Element source, Element target, double confidence) {
        return new ClassificationResult(source, target, confidence);
    }
}
