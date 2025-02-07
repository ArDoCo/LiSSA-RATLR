/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.postprocessor;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.kit.kastel.sdq.lissa.ratlr.knowledge.TraceLink;

public class ReqReqPostprocessor extends TraceLinkIdPostprocessor {
    @Override
    public Set<TraceLink> postprocess(Set<TraceLink> traceLinks) {
        // TraceLink[sourceId=UC10E1.txt, targetId=UC10E2.txt]
        // => TraceLink[sourceId=UC10E1, targetId=UC10E2]
        Set<TraceLink> result = new LinkedHashSet<>();
        for (TraceLink traceLink : traceLinks) {
            String sourceId = traceLink.sourceId();
            String targetId = traceLink.targetId();
            sourceId = sourceId.substring(0, sourceId.lastIndexOf("."));
            targetId = targetId.substring(0, targetId.lastIndexOf("."));
            result.add(new TraceLink(sourceId, targetId));
        }
        return result;
    }
}
