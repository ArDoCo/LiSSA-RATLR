/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.postprocessor;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.kit.kastel.sdq.lissa.ratlr.configuration.ModuleConfiguration;
import edu.kit.kastel.sdq.lissa.ratlr.context.ContextStore;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.TraceLink;

/**
 * Base class for postprocessors that modify trace link identifiers.
 * This class provides functionality to process trace links based on different
 * module configurations and ID processing strategies.
 * <p>
 * Postprocessors can access shared context via a {@link edu.kit.kastel.sdq.lissa.ratlr.context.ContextStore},
 * which is passed to their factory method.
 * </p>
 * The class supports various types of trace link processing:
 * <ul>
 *     <li>req2code: Requirements to code trace links</li>
 *     <li>sad2code: Software Architecture Documentation to code trace links</li>
 *     <li>sad2sam: Software Architecture Documentation to Software Architecture Model trace links</li>
 *     <li>sam2sad: Software Architecture Model to Software Architecture Documentation trace links</li>
 *     <li>sam2code: Software Architecture Model to code trace links</li>
 *     <li>req2req: Requirements to requirements trace links</li>
 *     <li>identity: No modification to trace links</li>
 * </ul>
 *
 * Each type of processing can have its own specific ID transformation rules,
 * implemented through the {@link IdProcessor} enum.
 */
public class TraceLinkIdPostprocessor {
    /** The ID processor used to transform trace link identifiers */
    private final IdProcessor idProcessor;

    /**
     * Creates a new trace link ID postprocessor with the specified ID processor.
     *
     * @param idProcessor The ID processor to use for transforming trace link identifiers
     */
    private TraceLinkIdPostprocessor(IdProcessor idProcessor) {
        this.idProcessor = idProcessor;
    }

    /**
     * Creates a new trace link ID postprocessor without an ID processor.
     * This constructor is intended for subclasses that override the postprocess method.
     */
    protected TraceLinkIdPostprocessor() {
        this.idProcessor = null;
    }

    /**
     * Creates a trace link ID postprocessor based on the module configuration.
     * The type of postprocessor is determined by the module name in the configuration.
     *
     * @param moduleConfiguration The module configuration specifying the type of postprocessor
     * @param contextStore The shared context store for pipeline components
     * @return A new trace link ID postprocessor instance
     * @throws IllegalStateException if the module name is not recognized
     */
    public static TraceLinkIdPostprocessor createTraceLinkIdPostprocessor(
            ModuleConfiguration moduleConfiguration, ContextStore contextStore) {
        return switch (moduleConfiguration.name()) {
            case "req2code" -> new TraceLinkIdPostprocessor(IdProcessor.REQ2CODE);
            case "sad2code" -> new TraceLinkIdPostprocessor(IdProcessor.SAD2CODE);
            case "sad2sam" -> new TraceLinkIdPostprocessor(IdProcessor.SAD2SAM);
            case "sam2sad" -> new TraceLinkIdPostprocessor(IdProcessor.SAM2SAD);
            case "sam2code" -> new TraceLinkIdPostprocessor(IdProcessor.SAM2CODE);
            case "req2req" -> new ReqReqPostprocessor();
            case "identity" -> new IdentityPostprocessor();
            case null -> new IdentityPostprocessor();
            default -> throw new IllegalStateException("Unexpected value: " + moduleConfiguration.name());
        };
    }

    /**
     * Postprocesses a set of trace links by applying the ID processor to each trace link.
     * This method can be overridden by subclasses to provide custom processing logic.
     *
     * @param traceLinks The set of trace links to process
     * @return A new set containing the processed trace links
     * @throws IllegalStateException if no ID processor is set and the method is not overridden
     */
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
