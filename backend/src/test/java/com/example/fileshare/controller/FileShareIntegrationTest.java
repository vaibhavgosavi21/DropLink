package com.example.fileshare.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileShareIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void uploadThenGetMetadataThenDownload() throws Exception {
        // 1. Upload a file
        MockMultipartFile file = new MockMultipartFile(
            "file", "hello.txt", "text/plain", "Hello, DropLink!".getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/files/upload").file(file))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.file.shareId").isNotEmpty())
            .andExpect(jsonPath("$.file.shareUrl").isNotEmpty())
            .andReturn();

        String shareId = objectMapper
            .readTree(uploadResult.getResponse().getContentAsString())
            .path("file").path("shareId").asText();

        assertThat(shareId).isNotBlank().hasSize(22);

        // 2. Get metadata by shareId
        mockMvc.perform(get("/api/files/share/{shareId}", shareId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.shareId").value(shareId))
            .andExpect(jsonPath("$.fileName").value("hello.txt"))
            .andExpect(jsonPath("$.contentType").value("text/plain"))
            .andExpect(jsonPath("$.fileSize").value(16))
            .andExpect(jsonPath("$.downloadCount").value(0));

        // 3. Download the file
        MvcResult downloadResult = mockMvc.perform(get("/api/files/share/{shareId}/download", shareId))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition",
                org.hamcrest.Matchers.containsString("attachment")))
            .andExpect(header().string("Content-Disposition",
                org.hamcrest.Matchers.containsString("hello.txt")))
            .andReturn();

        String body = downloadResult.getResponse().getContentAsString();
        assertThat(body).isEqualTo("Hello, DropLink!");

        // 4. Verify download count incremented
        mockMvc.perform(get("/api/files/share/{shareId}", shareId))
            .andExpect(jsonPath("$.downloadCount").value(1));
    }

    @Test
    void uploadMultipleFilesReturnsAllShareIds() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("files", "a.txt", "text/plain", "aaa".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "b.txt", "text/plain", "bbb".getBytes());

        MvcResult result = mockMvc.perform(multipart("/api/files/upload/multiple")
                .file(file1).file(file2))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.files").isArray())
            .andExpect(jsonPath("$.files.length()").value(2))
            .andReturn();

        JsonNode files = objectMapper
            .readTree(result.getResponse().getContentAsString())
            .path("files");

        String shareId1 = files.get(0).path("shareId").asText();
        String shareId2 = files.get(1).path("shareId").asText();

        assertThat(shareId1).isNotBlank().hasSize(22);
        assertThat(shareId2).isNotBlank().hasSize(22);
        assertThat(shareId1).isNotEqualTo(shareId2);
    }

    @Test
    void shareUrlContainsShareId() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "doc.pdf", "application/pdf", new byte[100]
        );

        MvcResult result = mockMvc.perform(multipart("/api/files/upload").file(file))
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode fileNode = objectMapper
            .readTree(result.getResponse().getContentAsString())
            .path("file");

        String shareId = fileNode.path("shareId").asText();
        String shareUrl = fileNode.path("shareUrl").asText();

        assertThat(shareUrl).endsWith("/share/" + shareId);
    }

    @Test
    void metadataReturns404ForUnknownShareId() throws Exception {
        mockMvc.perform(get("/api/files/share/nonexistentId123456789"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));
    }
}
