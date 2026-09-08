package com.example.fileshare.util;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public final class ExpirationParser {

    private static final Map<String, Duration> DURATIONS = Map.of(
        "1h",  Duration.ofHours(1),
        "6h",  Duration.ofHours(6),
        "24h", Duration.ofHours(24),
        "7d",  Duration.ofDays(7),
        "30d", Duration.ofDays(30)
    );

    private ExpirationParser() {}

    /**
     * Returns the expiry Instant for the given option, or null for "never".
     */
    public static Instant parse(String expiresIn) {
        if (expiresIn == null || "never".equalsIgnoreCase(expiresIn)) {
            return null;
        }
        Duration duration = DURATIONS.get(expiresIn.toLowerCase());
        if (duration == null) {
            throw new IllegalArgumentException("Invalid expiration option: " + expiresIn +
                ". Valid options: 1h, 6h, 24h, 7d, 30d, never");
        }
        return Instant.now().plus(duration);
    }
}
