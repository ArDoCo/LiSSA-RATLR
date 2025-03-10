/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.contextprovider;

import java.util.stream.Collectors;

import edu.kit.kastel.sdq.lissa.ratlr.elementstore.ElementStore;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;
import edu.kit.kastel.sdq.lissa.ratlr.utils.Pair;

// Currently complex contexts are not needed for the evaluation of LiSSA
public final class ContextProvider {
    private static final Pair<String, String> EMPTY_CONTEXT = new Pair<>("", "");

    private ContextProvider() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Provides the content of siblings of the given element.
     * The contents are concatenated into a before string and after string.
     */
    public static Pair<String, String> getNeighbors(ElementStore elementStore, Element element, int pre, int post) {
        if (pre <= 0 && post <= 0) return EMPTY_CONTEXT;

        if (element.getParent() == null) return EMPTY_CONTEXT;

        var siblings = elementStore.getElementsByParentId(element.getParent().getIdentifier()).stream()
                .map(Pair::first)
                .toList();
        // TODO ensure that they are sorted
        int index = siblings.indexOf(element);
        int preStart = Math.max(0, index - pre);
        int postEnd = Math.min(siblings.size(), index + post + 1);

        var preSiblings = siblings.subList(preStart, index);
        var postSiblings = siblings.subList(index + 1, postEnd);

        String preText = preSiblings.stream().map(Element::getContent).collect(Collectors.joining("\n"));
        String postText = postSiblings.stream().map(Element::getContent).collect(Collectors.joining("\n"));
        return new Pair<>(preText, postText);
    }
}
