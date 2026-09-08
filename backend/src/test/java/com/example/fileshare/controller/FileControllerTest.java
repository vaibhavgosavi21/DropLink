package com.example.fileshare.controller;

import com.example.fileshare.dto.DownloadResult;
import com.example.fileshare.dto.FileMetadataResponse;
import com.example.fileshare.dto.UploadedFileResponse;
import com.example.fileshare.exception.FileExpiredException;
import com.example.fileshare.exception.FileNotFoundException;
import com.example.fileshare.exception.InvalidFileException;
import com.example.fileshare.service.FileService;
import com.example.fileshare.service.FileUploadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.example.fileshare.config.SecurityConfig;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {FileController.class, HealthController.class})
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@WithMockUser
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileUploadService uploadService;

    @MockitoBean
    private FileService fileService;

    @Test
    void uploadReturns201WithShareUrl() throws Exception {
        UploadedFileResponse response = new UploadedFileResponse(
            "photo.jpg", 1024L, "image/jpeg", "abc123", "http://localhost:5173/share/abc123"
        );
        when(uploadService.upload(any(), eq("never"))).thenReturn(response);

        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[1024]);

        mockMvc.perform(multipart("/api/files/upload").file(file))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.file.shareId").value("abc123"))
            .andExpect(jsonPath("$.file.shareUrl").value("http://localhost:5173/share/abc123"));
    }

    @Test
    void uploadReturns400ForInvalidFile() throws Exception {
        when(uploadService.upload(any(), any())).thenThrow(new InvalidFileException("Unsupported file type: .exe"));

        MockMultipartFile file = new MockMultipartFile("file", "virus.exe", "application/octet-stream", new byte[10]);

        mockMvc.perform(multipart("/api/files/upload").file(file))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Unsupported file type: .exe"));
    }

    @Test
    void getMetadataReturns200() throws Exception {
        FileMetadataResponse meta = new FileMetadataResponse(
            "photo.jpg", "image/jpeg", 1024L, "AbCdEfGhIjKlMnOpQrStuv",
            Instant.now(), null, 0L
        );
        when(fileService.getFileMetadata("AbCdEfGhIjKlMnOpQrStuv")).thenReturn(meta);

        mockMvc.perform(get("/api/files/share/AbCdEfGhIjKlMnOpQrStuv"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.shareId").value("AbCdEfGhIjKlMnOpQrStuv"))
            .andExpect(jsonPath("$.fileName").value("photo.jpg"));
    }

    @Test
    void getMetadataReturns404ForUnknownShareId() throws Exception {
        when(fileService.getFileMetadata("AbCdEfGhIjKlMnOpQrStuv")).thenThrow(new FileNotFoundException("File not found"));

        mockMvc.perform(get("/api/files/share/AbCdEfGhIjKlMnOpQrStuv"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void downloadReturns200WithFileContent() throws Exception {
        var record = new com.example.fileshare.entity.FileRecord();
        record.setOriginalFileName("photo.jpg");
        record.setContentType("image/jpeg");
        record.setFileSize(5L);
        record.setDownloadCount(1L);

        ByteArrayResource resource = new ByteArrayResource("hello".getBytes());
        when(fileService.prepareDownload("AbCdEfGhIjKlMnOpQrStuv")).thenReturn(new DownloadResult(record, resource));

        mockMvc.perform(get("/api/files/share/AbCdEfGhIjKlMnOpQrStuv/download"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition",
                org.hamcrest.Matchers.containsString("attachment")))
            .andExpect(header().string("Content-Disposition",
                org.hamcrest.Matchers.containsString("photo.jpg")))
            .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }

    @Test
    void downloadReturns410ForExpiredLink() throws Exception {
        when(fileService.prepareDownload("AbCdEfGhIjKlMnOpQrStuv")).thenThrow(new FileExpiredException("This link has expired."));

        mockMvc.perform(get("/api/files/share/AbCdEfGhIjKlMnOpQrStuv/download"))
            .andExpect(status().isGone())
            .andExpect(jsonPath("$.message").value("This link has expired."));
    }
}
