/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.elementstore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.sdq.lissa.ratlr.configuration.ModuleConfiguration;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;
import edu.kit.kastel.sdq.lissa.ratlr.utils.Pair;

public class ElementStore {

    public static final String MAX_RESULTS_INFINITY_ARGUMENT = "infinity";
    private final Map<String, Pair<Element, float[]>> idToElementWithEmbedding;
    private final List<Pair<Element, float[]>> elementsWithEmbedding;
    private final int maxResults;

    /**
     * Creates a new element store.
     *
     * @param configuration       The configuration of the module.
     * @param similarityRetriever Whether the element store should be used as a retriever. If set to false, you can retrieve all elements. If set to true, you
     *                            can find similar elements.
     */
    public ElementStore(ModuleConfiguration configuration, boolean similarityRetriever) {
        if (similarityRetriever) {
            final String maxResultsKey = "max_results";
            boolean isInfinity = configuration.hasArgument(maxResultsKey)
                    && configuration.argumentAsString(maxResultsKey).equalsIgnoreCase(MAX_RESULTS_INFINITY_ARGUMENT);

            if (isInfinity) {
                this.maxResults = Integer.MAX_VALUE;
            } else {
                this.maxResults = configuration.argumentAsInt(maxResultsKey, 10);
                if (maxResults < 1) {
                    throw new IllegalArgumentException("The maximum number of results must be greater than 0.");
                }
            }
        } else {
            this.maxResults = -1;
        }

        elementsWithEmbedding = new ArrayList<>();
        idToElementWithEmbedding = new HashMap<>();
    }

    public void setup(List<Element> elements, List<float[]> embeddings) {
        if (!elementsWithEmbedding.isEmpty() || !idToElementWithEmbedding.isEmpty()) {
            throw new IllegalStateException("The element store is already set up.");
        }

        if (elements.size() != embeddings.size()) {
            throw new IllegalArgumentException("The number of elements and embeddings must be equal.");
        }

        for (int i = 0; i < elements.size(); i++) {
            var element = elements.get(i);
            var embedding = embeddings.get(i);
            var pair = new Pair<>(element, embedding);
            elementsWithEmbedding.add(pair);
            idToElementWithEmbedding.put(element.getIdentifier(), pair);
        }
    }

    public final List<Element> findSimilar(float[] queryVector) {
        return findSimilarWithDistances(queryVector).stream().map(Pair::first).toList();
    }

    public List<Pair<Element, Float>> findSimilarWithDistances(float[] queryVector) {
        if (maxResults < 0) {
            throw new IllegalStateException("You should set retriever to true to activate this feature.");
        }
        return findSimilarWithDistancesByCosineSimilarity(queryVector);
    }

    private List<Pair<Element, Float>> findSimilarWithDistancesByCosineSimilarity(float[] queryVector) {
        var elements = getAllElementsIntern(true);
        List<Pair<Element, Float>> similarElements = new ArrayList<>();
        for (var element : elements) {
            float[] elementVector = element.second();
            float similarity = cosineSimilarity(queryVector, elementVector);
            similarElements.add(new Pair<>(element.first(), similarity));
        }
        similarElements.sort((a, b) -> Float.compare(b.second(), a.second()));
        return similarElements.subList(0, Math.min(maxResults, similarElements.size()));
    }

    private float cosineSimilarity(float[] queryVector, float[] elementVector) {
        if (queryVector.length != elementVector.length) {
            throw new IllegalArgumentException("The length of the query vector and the element vector must be equal.");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < queryVector.length; i++) {
            dotProduct += queryVector[i] * elementVector[i];
            normA += Math.pow(queryVector[i], 2);
            normB += Math.pow(elementVector[i], 2);
        }
        return (float) (dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)));
    }

    public Pair<Element, float[]> getById(String id) {
        var element = idToElementWithEmbedding.get(id);
        if (element == null) {
            return null;
        }
        return new Pair<>(element.first(), Arrays.copyOf(element.second(), element.second().length));
    }

    public List<Pair<Element, float[]>> getElementsByParentId(String parentId) {
        List<Pair<Element, float[]>> elements = new ArrayList<>();
        for (Pair<Element, float[]> element : elementsWithEmbedding) {
            if (element.first().getParent() != null
                    && element.first().getParent().getIdentifier().equals(parentId)) {
                elements.add(new Pair<>(element.first(), Arrays.copyOf(element.second(), element.second().length)));
            }
        }
        return elements;
    }

    public List<Pair<Element, float[]>> getAllElements(boolean onlyCompare) {
        if (maxResults > 0) {
            throw new IllegalStateException("You should set retriever to false to activate this feature.");
        }
        return getAllElementsIntern(onlyCompare);
    }

    private List<Pair<Element, float[]>> getAllElementsIntern(boolean onlyCompare) {
        List<Pair<Element, float[]>> elements = new ArrayList<>();
        for (Pair<Element, float[]> element : elementsWithEmbedding) {
            if (!onlyCompare || element.first().isCompare()) {
                elements.add(new Pair<>(element.first(), Arrays.copyOf(element.second(), element.second().length)));
            }
        }
        return elements;
    }
}
