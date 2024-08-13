package edu.kit.kastel.sdq.lissa.ratlr.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class Cache {
    private static final Logger logger = LoggerFactory.getLogger(Cache.class);
    private static final int MAX_DIRTY = 50;
    private final File file;
    private final ObjectMapper mapper;
    private Map<String, String> data = new HashMap<>();
    private int dirty = 0;

    public Cache(String cacheFile) {
        file = new File(cacheFile);
        mapper = new ObjectMapper();
        if (file.exists()) {
            try {
                data = mapper.readValue(file, new TypeReference<Map<String, String>>() {
                });
            } catch (IOException e) {
                throw new IllegalArgumentException("Could not read cache file (" + file.getName() + ")", e);
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (dirty > 0) {
                write();
            }
        }));
    }

    public File getFile() {
        return file;
    }

    private void write() {
        try {
            File tempFile = new File(file.getAbsolutePath() + ".tmp.json");
            mapper.writeValue(tempFile, data);
            Files.copy(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.delete(tempFile.toPath());
            dirty = 0;
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not write cache file", e);
        }
    }

    public synchronized void put(String key, String value) {
        String old = data.put(key, value);
        if (old == null || !old.equals(value)) {
            dirty++;
        }

        if (dirty > MAX_DIRTY) {
            write();
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T get(String key, Class<T> clazz) {
        try {
            var jsonData = this.data.get(key);
            if (jsonData == null) {
                return null;
            }

            if (clazz == String.class) {
                return (T) jsonData;
            }

            return mapper.readValue(jsonData, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not deserialize object", e);
        }
    }

    public synchronized <T> void put(String key, T value) {
        try {
            put(key, mapper.writeValueAsString(Objects.requireNonNull(value)));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not serialize object", e);
        }
    }

    /**
     * Merges another cache into this one and updates the cache file. If a key already exists the value of the other cache will be taken.
     * 
     * @param other the other cache to retrieve the information to merge from
     */
    private void addAllEntriesInternal(Cache other) {
        this.data.putAll(other.data);
        write();
    }

    /**
     * Merges another cache into this one if no existing key gets overridden.
     * Overriding means updating an existing value into a different one.
     * If {@code keyCollector} still is empty after invoking this method, the contents have been merged.
     * If it wasn't empty to begin with, merging can never happen.
     * 
     * @param other      the other cache to retrieve the information to merge from
     * @param forceMerge if true, the other cache will be merged into this cache, even if keys would be overridden
     * @return the collection to which all keys of the other cache are added, which would have overridden existing keys. If not empty, merging is not possible.
     *
     */
    public synchronized Set<String> addAllEntries(Cache other, boolean forceMerge) {
        Set<String> keyCollector = new TreeSet<>();
        Map<String, String> thisData = new HashMap<>(this.data);
        Map<String, String> otherData = new HashMap<>(other.data);

        Set<String> allKeys = new HashSet<>(thisData.keySet());
        allKeys.addAll(otherData.keySet());

        for (String key : allKeys) {
            String thisValue = thisData.get(key);
            String otherValue = otherData.get(key);
            if (Objects.equals(thisValue, otherValue)) {
                // Equal values are fine
                continue;
            }
            if (thisValue == null || otherValue == null) {
                // One value is null, the other is not .. this is also fine
                continue;
            }
            logger.warn("Key '{}' would have been overridden by other cache ({})", key, this.file.getName());
            logger.debug("  Current value: '{}'", thisValue);
            logger.debug("  Other value: '{}'", otherValue);
            keyCollector.add(key);
        }
        if (keyCollector.isEmpty() || forceMerge) {
            addAllEntriesInternal(other);
        }
        return keyCollector;
    }
}
