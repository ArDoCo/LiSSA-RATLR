/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.cache;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

class LocalCache {
    private final ObjectMapper mapper;

    private static final int MAX_DIRTY = 50;
    private int dirty = 0;

    private final File cacheFile;
    private Map<String, String> cache = new HashMap<>();

    LocalCache(String cacheFile) {
        this.cacheFile = new File(cacheFile);
        mapper = new ObjectMapper();
        createLocalStore();
    }

    public boolean isReady() {
        return cacheFile.exists() || cacheFile.canWrite();
    }

    private void createLocalStore() {
        if (cacheFile.exists()) {
            try {
                cache = mapper.readValue(cacheFile, new TypeReference<>() {});
            } catch (IOException e) {
                throw new IllegalArgumentException("Could not read cache file (" + cacheFile.getName() + ")", e);
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (dirty > 0) {
                write();
            }
        }));
    }

    public synchronized void write() {
        try {
            File tempFile = new File(cacheFile.getAbsolutePath() + ".tmp.json");
            mapper.writeValue(tempFile, cache);
            Files.copy(tempFile.toPath(), cacheFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.delete(tempFile.toPath());
            dirty = 0;
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not write cache file", e);
        }
    }

    public synchronized String get(CacheKey key) {
        return cache.get(key.localKey());
    }

    public synchronized void put(CacheKey key, String value) {
        String old = cache.put(key.localKey(), value);
        if (old == null || !old.equals(value)) {
            dirty++;
        }

        if (dirty > MAX_DIRTY) {
            write();
        }
    }
}
