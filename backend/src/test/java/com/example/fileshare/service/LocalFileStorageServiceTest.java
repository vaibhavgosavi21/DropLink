package com.example.fileshare.service;

import com.example.fileshare.exception.FileStorageException;
import com.example.fileshare.service.storage.LocalFileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalFileStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new LocalFileStorageService();
        ReflectionTestUtils.setField(storageService, "uploadDir", tempDir.toString());
        storageService.init();
    }

    @Test
    void storesFileSuccessfully() {
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain", "hello".getBytes());
        storageService.store(file, "stored-hello.txt");
        assertThat(storageService.exists("stored-hello.txt")).isTrue();
    }

    @Test
    void loadsStoredFileAsReadableResource() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.txt", "text/plain", "content".getBytes());
        storageService.store(file, "doc.txt");

        Resource resource = storageService.load("doc.txt");
        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();
    }

    @Test
    void deletesFileSuccessfully() {
        MockMultipartFile file = new MockMultipartFile("file", "del.txt", "text/plain", "bye".getBytes());
        storageService.store(file, "del.txt");
        assertThat(storageService.exists("del.txt")).isTrue();

        storageService.delete("del.txt");
        assertThat(storageService.exists("del.txt")).isFalse();
    }

    @Test
    void deleteNonExistentFileDoesNotThrow() {
        storageService.delete("ghost.txt");
    }

    @Test
    void existsReturnsFalseForMissingFile() {
        assertThat(storageService.exists("missing.txt")).isFalse();
    }

    @Test
    void loadThrowsForNonExistentFile() {
        assertThatThrownBy(() -> storageService.load("nonexistent.txt"))
            .isInstanceOf(FileStorageException.class);
    }

    @Test
    void storeThrowsOnPathTraversal() {
        MockMultipartFile file = new MockMultipartFile("file", "evil.txt", "text/plain", "x".getBytes());
        assertThatThrownBy(() -> storageService.store(file, "../evil.txt"))
            .isInstanceOf(FileStorageException.class)
            .hasMessageContaining("Path traversal");
    }

    @Test
    void loadThrowsOnPathTraversal() {
        assertThatThrownBy(() -> storageService.load("../../etc/passwd"))
            .isInstanceOf(FileStorageException.class)
            .hasMessageContaining("Path traversal");
    }

    @Test
    void deleteThrowsOnPathTraversal() {
        assertThatThrownBy(() -> storageService.delete("../secret.txt"))
            .isInstanceOf(FileStorageException.class)
            .hasMessageContaining("Path traversal");
    }

    @Test
    void existsThrowsOnPathTraversal() {
        assertThatThrownBy(() -> storageService.exists("../../windows/system32"))
            .isInstanceOf(FileStorageException.class)
            .hasMessageContaining("Path traversal");
    }
}
