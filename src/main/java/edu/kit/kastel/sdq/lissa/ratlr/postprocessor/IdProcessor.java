/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.postprocessor;

import java.util.function.Function;

import edu.kit.kastel.sdq.lissa.ratlr.knowledge.TraceLink;

enum IdProcessor {
    REQ2CODE(
            sourceId -> sourceId.substring(0, sourceId.indexOf(".")),
            targetId -> targetId.substring(0, targetId.indexOf("."))),
    SAD2CODE(IdProcessor::processSAD, targetId -> targetId),
    SAM2SAD(IdProcessor::processSAM, IdProcessor::processSAD),
    SAD2SAM(IdProcessor::processSAD, IdProcessor::processSAM),
    SAM2CODE(IdProcessor::processSAM, targetId -> targetId);

    private final Function<String, String> sourceIdProcessor;
    private final Function<String, String> targetIdProcessor;

    IdProcessor(Function<String, String> sourceIdProcessor, Function<String, String> targetIdProcessor) {
        this.sourceIdProcessor = sourceIdProcessor;
        this.targetIdProcessor = targetIdProcessor;
    }

    public TraceLink process(TraceLink traceLink) {
        return new TraceLink(
                sourceIdProcessor.apply(traceLink.sourceId()), targetIdProcessor.apply(traceLink.targetId()));
    }

    private static String processSAD(String sadID) {
        return String.valueOf(Integer.parseInt(sadID.substring(sadID.lastIndexOf("$") + 1)) + 1);
    }

    private static String processSAM(String samID) {
        return samID.substring(samID.lastIndexOf("$") + 1);
    }
}
