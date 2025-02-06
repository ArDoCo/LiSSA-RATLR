/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.postprocessor;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.kit.kastel.sdq.lissa.ratlr.knowledge.TraceLink;

public class SadCodePostprocessor extends TraceLinkIdPostprocessor {
    @Override
    public Set<TraceLink> postprocess(Set<TraceLink> traceLinks) {
        // TraceLink[sourceId=mediastore.txt$0,
        // targetId=Implementation/mediastore.web/src/edu/kit/ipd/sdq/mediastore/web/beans/UploadBean.java]
        // => TraceLink[sourceId=1,
        // targetId=Implementation/mediastore.web/src/edu/kit/ipd/sdq/mediastore/web/beans/UploadBean.java]
        Set<TraceLink> result = new LinkedHashSet<>();
        for (TraceLink traceLink : traceLinks) {
            String sourceId = traceLink.sourceId();
            String targetId = traceLink.targetId();
            sourceId = sourceId.substring(sourceId.indexOf("$") + 1);
            sourceId = String.valueOf(Integer.parseInt(sourceId) + 1);
            result.add(new TraceLink(sourceId, targetId));
        }
        return result;
    }
}
