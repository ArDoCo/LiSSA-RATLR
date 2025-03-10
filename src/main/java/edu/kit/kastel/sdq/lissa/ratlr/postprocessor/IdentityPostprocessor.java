/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.postprocessor;

import java.util.Set;

import edu.kit.kastel.sdq.lissa.ratlr.knowledge.TraceLink;

public class IdentityPostprocessor extends TraceLinkIdPostprocessor {

    @Override
    public Set<TraceLink> postprocess(Set<TraceLink> traceLinks) {
        return traceLinks;
    }
}
