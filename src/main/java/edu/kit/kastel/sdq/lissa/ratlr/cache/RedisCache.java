/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.cache;

import java.time.Instant;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.UnifiedJedis;

class RedisCache implements Cache {
    private static final Logger logger = LoggerFactory.getLogger(RedisCache.class);
    private final ObjectMapper mapper;

    private final LocalCache localCache;
    private UnifiedJedis jedis;

    RedisCache(LocalCache localCache) {
        this.localCache = localCache == null || !localCache.isReady() ? null : localCache;
        mapper = new ObjectMapper();
        createRedisConnection();
        if (jedis == null && localCache == null) {
            throw new IllegalArgumentException("Could not create cache");
        }
    }

    private void createRedisConnection() {
        try {
            String redisUrl = "redis://localhost:6379";
            if (System.getenv("REDIS_URL") != null) {
                redisUrl = System.getenv("REDIS_URL");
            }
            jedis = new UnifiedJedis(redisUrl);
            // Check if connection is working
            jedis.ping();
        } catch (Exception e) {
            logger.warn("Could not connect to Redis, using file cache instead");
            jedis = null;
        }
    }

    @Override
    public synchronized <T> T get(CacheKey key, Class<T> clazz) {
        var jsonData = jedis == null ? null : jedis.hget(key.toRawKey(), "data");
        if (jsonData == null && localCache != null) {
            jsonData = localCache.get(key);
            if (jedis != null && jsonData != null) {
                jedis.set(key.toRawKey(), jsonData);
            }
        }

        return convert(jsonData, clazz);
    }

    @SuppressWarnings("unchecked")
    private <T> T convert(String jsonData, Class<T> clazz) {
        if (jsonData == null) {
            return null;
        }
        if (clazz == String.class) {
            return (T) jsonData;
        }

        try {
            return mapper.readValue(jsonData, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not deserialize object", e);
        }
    }

    @Override
    public synchronized void put(CacheKey key, String value) {
        if (jedis != null) {
            String rawKey = key.toRawKey();
            jedis.hset(rawKey, "data", value);
            jedis.hset(rawKey, "timestamp", String.valueOf(Instant.now().getEpochSecond()));
        }
        if (localCache != null) {
            localCache.put(key, value);
        }
    }

    @Override
    public synchronized <T> void put(CacheKey key, T value) {
        try {
            put(key, mapper.writeValueAsString(Objects.requireNonNull(value)));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not serialize object", e);
        }
    }
}
