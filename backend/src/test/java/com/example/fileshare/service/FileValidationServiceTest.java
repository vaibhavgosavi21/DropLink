package com.example.fileshare.service;

import com.example.fileshare.config.FileProperties;
import com.example.fileshare.exception.FileSizeExceededException;
import com.example.fileshare.exception.InvalidFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class FileValidationServiceTest {

    private FileValidationService validationService;
    private FileProperties fileProperties;

    @BeforeEach
    void setUp() {
        fileProperties = Mockito.mock(FileProperties.class);
        when(fileProperties.getMaxSize()).thenReturn(10 * 1024 * 1024L); // 10 MB
        validationService = new FileValidationService(fileProperties);
    }

    @Test
    void acceptsValidImageFile() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[100]);
        validationService.validate(file);
    }

    @Test
    void acceptsValidPdfFile() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[100]);
        validationService.validate(file);
    }

    @Test
    void rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);
        assertThatThrownBy(() -> validationService.validate(file))
            .isInstanceOf(InvalidFileException.class)
            .hasMessageContaining("empty");
    }

    @Test
    void rejectsMissingFileName() {
        MockMultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[10]);
        assertThatThrownBy(() -> validationService.validate(file))
            .isInstanceOf(InvalidFileException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"malware.exe", "script.bat", "hack.sh", "virus.ps1"})
    void rejectsBlockedExtensions(String filename) {
        MockMultipartFile file = new MockMultipartFile("file", filename, "application/octet-stream", new byte[10]);
        assertThatThrownBy(() -> validationService.validate(file))
            .isInstanceOf(InvalidFileException.class)
            .hasMessageContaining("not permitted");
    }

    @Test
    void rejectsUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "file.xyz", "application/octet-stream", new byte[10]);
        assertThatThrownBy(() -> validationService.validate(file))
            .isInstanceOf(InvalidFileException.class)
            .hasMessageContaining("Unsupported");
    }

    @Test
    void rejectsOversizedFile() {
        byte[] bigFile = new byte[11 * 1024 * 1024]; // 11 MB
        MockMultipartFile file = new MockMultipartFile("file", "big.zip", "application/zip", bigFile);
        assertThatThrownBy(() -> validationService.validate(file))
            .isInstanceOf(FileSizeExceededException.class);
    }

    @Test
    void sanitizesPathTraversalInFileName() {
        String sanitized = validationService.sanitizeFileName("../../etc/passwd.txt");
        assertThat(sanitized).doesNotContain("..");
        assertThat(sanitized).doesNotContain("/");
    }

    @Test
    void sanitizesSpecialCharacters() {
        String sanitized = validationService.sanitizeFileName("my file (1).pdf");
        assertThat(sanitized).doesNotContain(" ");
        assertThat(sanitized).doesNotContain("(");
        assertThat(sanitized).endsWith(".pdf");
    }
}
