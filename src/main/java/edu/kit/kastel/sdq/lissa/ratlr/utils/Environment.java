/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.utils;

import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.cdimascio.dotenv.Dotenv;

public final class Environment {
    private static final Logger logger = LoggerFactory.getLogger(Environment.class);
    private static final Dotenv DOTENV = load();

    private Environment() {
        throw new IllegalAccessError("Utility class");
    }

    public static String getenv(String key) {
        String dotenvValue = DOTENV == null ? null : DOTENV.get(key);
        if (dotenvValue != null) return dotenvValue;
        return System.getenv(key);
    }

    public static String getenvNonNull(String key) {
        String env = getenv(key);
        if (env == null) {
            logger.error("environment variable {} is missing, use '.env' or your system to set it up", key);
        }
        return env;
    }

    private static synchronized Dotenv load() {
        if (DOTENV != null) {
            return DOTENV;
        }

        if (Files.exists(Path.of(".env"))) {
            return Dotenv.configure().load();
        } else {
            logger.info("No .env file found, using system environment variables");
            return null;
        }
    }
}
