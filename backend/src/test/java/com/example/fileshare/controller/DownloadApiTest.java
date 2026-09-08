package com.example.fileshare.controller;

import com.example.fileshare.entity.FileRecord;
import com.example.fileshare.repository.FileRepository;
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
class DownloadApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileRepository fileRepository;

    @Test
    void downloadReturnsCorrectHeaders() throws Exception {
        String shareId = uploadAndGetShareId("report.pdf", "application/pdf", "PDF content".getBytes());

        mockMvc.perform(get("/api/files/share/{shareId}/download", shareId))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition",
                org.hamcrest.Matchers.containsString("attachment")))
            .andExpect(header().string("Content-Disposition",
                org.hamcrest.Matchers.containsString("report.pdf")))
            .andExpect(header().exists("Content-Length"))
            .andExpect(content().contentType("application/pdf"));
    }

    @Test
    void downloadIncrementsCountOnEachRequest() throws Exception {
        String shareId = uploadAndGetShareId("counter.txt", "text/plain", "data".getBytes());

        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(get("/api/files/share/{shareId}/download", shareId))
                .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/files/share/{shareId}", shareId))
            .andExpect(jsonPath("$.downloadCount").value(3));
    }

    @Test
    void downloadReturns410ForExpiredFile() throws Exception {
        String shareId = uploadAndGetShareId("expired.txt", "text/plain", "bye".getBytes());

        // Manually expire the record
        FileRecord record = fileRepository.findByShareId(shareId).orElseThrow();
        record.setExpiresAt(Instant.now().minusSeconds(1));
        fileRepository.save(record);

        mockMvc.perform(get("/api/files/share/{shareId}/download", shareId))
            .andExpect(status().isGone())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("This link has expired."));
    }

    @Test
    void downloadReturns404ForInactiveFile() throws Exception {
        String shareId = uploadAndGetShareId("deleted.txt", "text/plain", "gone".getBytes());

        // Soft-delete the record
        FileRecord record = fileRepository.findByShareId(shareId).orElseThrow();
        record.setActive(false);
        fileRepository.save(record);

        mockMvc.perform(get("/api/files/share/{shareId}/download", shareId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void downloadReturns404ForUnknownShareId() throws Exception {
        mockMvc.perform(get("/api/files/share/AbCdEfGhIjKlMnOpQrSt12/download"))
            .andExpect(status().isNotFound());
    }

    @Test
    void downloadReturns400ForInvalidShareIdFormat() throws Exception {
        mockMvc.perform(get("/api/files/share/../etc/passwd/download"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void downloadPreservesOriginalFileContent() throws Exception {
        byte[] content = "Hello World from DropLink!".getBytes();
        String shareId = uploadAndGetShareId("hello.txt", "text/plain", content);

        MvcResult result = mockMvc.perform(get("/api/files/share/{shareId}/download", shareId))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(content);
    }

    private String uploadAndGetShareId(String filename, String contentType, byte[] content) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", filename, contentType, content);
        MvcResult result = mockMvc.perform(multipart("/api/files/upload").file(file))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper
            .readTree(result.getResponse().getContentAsString())
            .path("file").path("shareId").asText();
    }
}
