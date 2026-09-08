package com.example.fileshare.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import java.time.temporal.ChronoUnit;

class ExpirationParserTest {

    @ParameterizedTest
    @ValueSource(strings = {"never", "NEVER", "Never"})
    void returnsNullForNeverVariants(String input) {
        assertThat(ExpirationParser.parse(input)).isNull();
    }

    @Test
    void returnsNullForNullInput() {
        assertThat(ExpirationParser.parse(null)).isNull();
    }

    @ParameterizedTest
    @CsvSource({
        "1h,  3600",
        "6h,  21600",
        "24h, 86400",
        "7d,  604800",
        "30d, 2592000"
    })
    void parsesValidOptionsToCorrectFutureInstant(String option, long expectedSeconds) {
        Instant before = Instant.now();
        Instant result = ExpirationParser.parse(option);
        Instant after = Instant.now();

        assertThat(result).isNotNull();
        // result should be approximately now + expectedSeconds
        assertThat(result).isAfter(before.plusSeconds(expectedSeconds - 5));
        assertThat(result).isBefore(after.plusSeconds(expectedSeconds + 5));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2h", "48h", "1d", "invalid", "0"})
    void throwsForInvalidOptions(String input) {
        assertThatThrownBy(() -> ExpirationParser.parse(input))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid expiration option");
    }
}
