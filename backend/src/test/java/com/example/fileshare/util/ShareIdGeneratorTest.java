package com.example.fileshare.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ShareIdGeneratorTest {

    @Test
    void generatesNonNullNonBlankId() {
        String id = ShareIdGenerator.generate();
        assertThat(id).isNotNull().isNotBlank();
    }

    @Test
    void generatedIdIsUrlSafe() {
        for (int i = 0; i < 100; i++) {
            String id = ShareIdGenerator.generate();
            // URL-safe Base64 uses only A-Z a-z 0-9 - _
            assertThat(id).matches("[A-Za-z0-9\\-_]+");
        }
    }

    @Test
    void generatedIdHasExpectedLength() {
        // 16 bytes → 22 chars in Base64 URL without padding
        String id = ShareIdGenerator.generate();
        assertThat(id).hasSize(22);
    }

    @Test
    void generatesUniqueIds() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            ids.add(ShareIdGenerator.generate());
        }
        assertThat(ids).hasSize(1000);
    }
}
