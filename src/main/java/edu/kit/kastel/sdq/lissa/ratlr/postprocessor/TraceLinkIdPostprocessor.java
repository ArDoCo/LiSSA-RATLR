/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.postprocessor;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.kit.kastel.sdq.lissa.ratlr.configuration.ModuleConfiguration;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.TraceLink;

public class TraceLinkIdPostprocessor {
    private final IdProcessor idProcessor;

    private TraceLinkIdPostprocessor(IdProcessor idProcessor) {
        this.idProcessor = idProcessor;
    }

    protected TraceLinkIdPostprocessor() {
        this.idProcessor = null;
    }

    public static TraceLinkIdPostprocessor createTraceLinkIdPostprocessor(ModuleConfiguration moduleConfiguration) {
        return switch (moduleConfiguration.name()) {
            case "req2code" -> new TraceLinkIdPostprocessor(IdProcessor.REQ2CODE);
            case "sad2code" -> new TraceLinkIdPostprocessor(IdProcessor.SAD2CODE);
            case "sad2sam" -> new TraceLinkIdPostprocessor(IdProcessor.SAD2SAM);
            case "sam2sad" -> new TraceLinkIdPostprocessor(IdProcessor.SAM2SAD);
            case "sam2code" -> new TraceLinkIdPostprocessor(IdProcessor.SAM2CODE);
            case "req2req" -> new ReqReqPostprocessor();
            case "identity" -> new IdentityPostprocessor(moduleConfiguration);
            case null -> new IdentityPostprocessor(moduleConfiguration);
            default -> throw new IllegalStateException("Unexpected value: " + moduleConfiguration.name());
        };
    }

    public Set<TraceLink> postprocess(Set<TraceLink> traceLinks) {
        if (idProcessor == null) {
            throw new IllegalStateException("idProcessor not set or method not overridden");
        }
        Set<TraceLink> result = new LinkedHashSet<>();
        for (TraceLink traceLink : traceLinks) {
            result.add(idProcessor.process(traceLink));
        }
        return result;
    }
}
