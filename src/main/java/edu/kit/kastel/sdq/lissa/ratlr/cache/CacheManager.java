package edu.kit.kastel.sdq.lissa.ratlr.cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class CacheManager {
    public static final String DEFAULT_CACHE_DIRECTORY = "cache";

    private static CacheManager DEFAULT_INSTANCE_MANAGER;
    private final Path directoryOfCaches;
    private final Map<String, Cache> caches = new HashMap<>();

    public static synchronized void setCacheDir(String directory) throws IOException {
        if (DEFAULT_INSTANCE_MANAGER != null) {
            throw new IllegalStateException("Cache directory already set");
        }
        DEFAULT_INSTANCE_MANAGER = new CacheManager(Path.of(directory == null ? DEFAULT_CACHE_DIRECTORY : directory));
    }

    /**
     * Creates a new instance using the specified cache directory.
     * As this constructor is designed to provide a cache manager for different cache sources, the directory is expected to already exist.
     * Use {@link CacheManager#getDefaultInstance()} to access the default cache directory.
     * 
     * @param cacheDir the cache directory to use
     * @throws IllegalArgumentException if the default cache directory is provided or cacheDir is not a directory
     */
    public CacheManager(Path cacheDir) throws IOException {
        if (!Files.exists(cacheDir))
            Files.createDirectory(cacheDir);
        if (!Files.isDirectory(cacheDir)) {
            throw new IllegalArgumentException("path is not a directory: " + cacheDir);
        }
        this.directoryOfCaches = cacheDir;

    }

    public static CacheManager getDefaultInstance() {
        if (DEFAULT_INSTANCE_MANAGER == null)
            throw new IllegalStateException("Cache directory not set");
        return DEFAULT_INSTANCE_MANAGER;
    }

    /**
     * Returns the cache for the name. Designed for model intern purposes.
     * 
     * @param name the name of the cache without file ending
     * @return the cache for the name
     */
    public Cache getCache(String name) {
        return getCache(name, true);
    }

    private Cache getCache(String name, boolean appendEnding) {
        name = name.replace(":", "__");

        if (caches.containsKey(name)) {
            return caches.get(name);
        }

        Cache cache = new Cache(directoryOfCaches + "/" + name + (appendEnding ? ".json" : ""));
        caches.put(name, cache);
        return cache;
    }

    /**
     * Returns the cache for an existing file.
     * 
     * @param path   the path pointing to the existing cache file
     * @param create whether the cache file should be created if it doesn't exist
     * @return the cache for an existing file
     * @throws IllegalArgumentException if the file does not exist or is a directory
     */
    public Cache getCache(Path path, boolean create) {
        path = directoryOfCaches.resolve(path.getFileName());
        if ((!create && Files.notExists(path)) || Files.isDirectory(path)) {
            throw new IllegalArgumentException("file does not exist or is a directory: " + path);
        }
        return getCache(path.getFileName().toString(), false);
    }
}
