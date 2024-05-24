package edu.kit.kastel.sdq.lissa.ratlr.preprocessor;

import edu.kit.kastel.sdq.lissa.ratlr.cache.Cache;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Artifact;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Element;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class CachedPreprocessor extends Preprocessor {
    private Cache cache;

    @Override
    public final List<Element> preprocess(Artifact artifact) {
        if (cache == null) {
            this.cache = Objects.requireNonNull(createCache());
        }

        String key = UUID.nameUUIDFromBytes(artifact.getContent().getBytes(StandardCharsets.UTF_8)).toString();

        Preprocessed cachedPreprocessed = cache.get(key, Preprocessed.class);
        if (cachedPreprocessed != null) {
            return cachedPreprocessed.elements();
        }

        Preprocessed preprocessed = new Preprocessed(preprocessIntern(artifact));
        cache.put(key, preprocessed);
        return preprocessed.elements();
    }

    protected abstract Cache createCache();

    protected abstract List<Element> preprocessIntern(Artifact artifact);

    private record Preprocessed(List<Element> elements) {
        public List<Element> elements() {
            this.elements.forEach(it -> it.init(elements.stream().collect(Collectors.toMap(Element::getIdentifier, Function.identity()))));
            return elements;
        }
    }
}
