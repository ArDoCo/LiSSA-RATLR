/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.cache;

public interface Cache {
    <T> T get(CacheKey key, Class<T> clazz);

    void put(CacheKey key, String value);

    <T> void put(CacheKey key, T value);
}
