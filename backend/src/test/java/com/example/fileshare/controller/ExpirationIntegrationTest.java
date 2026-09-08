package com.example.fileshare.controller;

import com.example.fileshare.entity.FileRecord;
import com.example.fileshare.repository.FileRepository;
import com.example.fileshare.service.FileCleanupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpirationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileCleanupService cleanupService;

    @Test
    void uploadWithExpiryStoresExpiresAt() throws Exception {
        String shareId = uploadAndGetShareId("7d");

        FileRecord record = fileRepository.findByShareId(shareId).orElseThrow();
        assertThat(record.getExpiresAt()).isNotNull();
        assertThat(record.getExpiresAt()).isAfter(Instant.now().plusSeconds(6 * 24 * 3600));
    }

    @Test
    void uploadWithNeverExpiryStoresNullExpiresAt() throws Exception {
        String shareId = uploadAndGetShareId("never");

        FileRecord record = fileRepository.findByShareId(shareId).orElseThrow();
        assertThat(record.getExpiresAt()).isNull();
    }

    @Test
    void fileIsAccessibleBeforeExpiry() throws Exception {
        String shareId = uploadAndGetShareId("24h");

        mockMvc.perform(get("/api/files/share/{shareId}", shareId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.shareId").value(shareId));
    }

    @Test
    void expiredFileReturns410OnMetadataAccess() throws Exception {
        String shareId = uploadAndGetShareId("1h");

        // Manually expire
        FileRecord record = fileRepository.findByShareId(shareId).orElseThrow();
        record.setExpiresAt(Instant.now().minusSeconds(1));
        fileRepository.save(record);

        mockMvc.perform(get("/api/files/share/{shareId}", shareId))
            .andExpect(status().isGone())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("This link has expired."));
    }

    @Test
    void expiredFileReturns410OnDownload() throws Exception {
        String shareId = uploadAndGetShareId("1h");

        FileRecord record = fileRepository.findByShareId(shareId).orElseThrow();
        record.setExpiresAt(Instant.now().minusSeconds(1));
        fileRepository.save(record);

        mockMvc.perform(get("/api/files/share/{shareId}/download", shareId))
            .andExpect(status().isGone());
    }

    @Test
    void cleanupDeactivatesExpiredRecords() throws Exception {
        String shareId = uploadAndGetShareId("1h");

        FileRecord record = fileRepository.findByShareId(shareId).orElseThrow();
        record.setExpiresAt(Instant.now().minusSeconds(1));
        fileRepository.save(record);

        int cleaned = cleanupService.cleanupExpiredFiles();
        assertThat(cleaned).isGreaterThanOrEqualTo(1);

        FileRecord after = fileRepository.findByShareId(shareId).orElseThrow();
        assertThat(after.isActive()).isFalse();
    }

    @Test
    void cleanupDoesNotAffectNonExpiredFiles() throws Exception {
        String shareId = uploadAndGetShareId("never");

        cleanupService.cleanupExpiredFiles();

        FileRecord record = fileRepository.findByShareId(shareId).orElseThrow();
        assertThat(record.isActive()).isTrue();
    }

    @Test
    void invalidExpiryOptionReturns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "data".getBytes());

        mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .param("expiresIn", "2h"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid expiration option")));
    }

    @Test
    void allValidExpiryOptionsAreAccepted() throws Exception {
        for (String option : new String[]{"1h", "6h", "24h", "7d", "30d", "never"}) {
            MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "data".getBytes());
            mockMvc.perform(multipart("/api/files/upload")
                    .file(file)
                    .param("expiresIn", option))
                .andExpect(status().isCreated());
        }
    }

    private String uploadAndGetShareId(String expiresIn) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "expire-test.txt", "text/plain", "content".getBytes());
        MvcResult result = mockMvc.perform(multipart("/api/files/upload")
                .file(file)
                .param("expiresIn", expiresIn))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper
            .readTree(result.getResponse().getContentAsString())
            .path("file").path("shareId").asText();
    }
}
