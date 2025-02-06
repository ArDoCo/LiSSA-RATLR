/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.utils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class KeyGenerator {
    private KeyGenerator() {
        throw new IllegalAccessError("Utility class");
    }

    public static String generateKey(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input must not be null");
        }
        // Normalize lineendings
        String normalized = input.replace("\r\n", "\n");
        return UUID.nameUUIDFromBytes(normalized.getBytes(StandardCharsets.UTF_8))
                .toString();
    }
}
