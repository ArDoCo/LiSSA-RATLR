/* Licensed under MIT 2025. */
package edu.kit.kastel.sdq.lissa.ratlr.utils;

import java.util.concurrent.Future;

import org.slf4j.Logger;

public final class Futures {
    private Futures() {
        throw new IllegalAccessError("Utility class");
    }

    @SuppressWarnings("java:S2139")
    public static <T> T getLogged(Future<T> future, Logger logger) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for future", e);
            Thread.currentThread().interrupt(); // Restore the interrupted status
            throw new IllegalStateException("Thread was interrupted while waiting for future result", e);
        } catch (Exception e) {
            logger.error("Error while getting future result: {}", e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }
}
