package com.example.fileshare.service;

import com.example.fileshare.entity.FileRecord;
import com.example.fileshare.repository.FileRepository;
import com.example.fileshare.service.storage.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileCleanupServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private FileStorageService storageService;

    @InjectMocks
    private FileCleanupService cleanupService;

    @Test
    void cleansUpExpiredFiles() {
        FileRecord expired1 = buildRecord("share-1", "stored-1.txt");
        FileRecord expired2 = buildRecord("share-2", "stored-2.txt");

        when(fileRepository.findAllByIsActiveAndExpiresAtBefore(eq(true), any(Instant.class)))
            .thenReturn(List.of(expired1, expired2));

        int count = cleanupService.cleanupExpiredFiles();

        assertThat(count).isEqualTo(2);
        assertThat(expired1.isActive()).isFalse();
        assertThat(expired2.isActive()).isFalse();
        verify(storageService).delete("stored-1.txt");
        verify(storageService).delete("stored-2.txt");
        verify(fileRepository, times(2)).save(any(FileRecord.class));
    }

    @Test
    void doesNothingWhenNoExpiredFiles() {
        when(fileRepository.findAllByIsActiveAndExpiresAtBefore(eq(true), any(Instant.class)))
            .thenReturn(List.of());

        int count = cleanupService.cleanupExpiredFiles();

        assertThat(count).isEqualTo(0);
        verifyNoInteractions(storageService);
        verify(fileRepository, never()).save(any());
    }

    @Test
    void deactivatesRecordBeforeDeletingFromStorage() {
        FileRecord record = buildRecord("share-x", "stored-x.txt");
        when(fileRepository.findAllByIsActiveAndExpiresAtBefore(eq(true), any(Instant.class)))
            .thenReturn(List.of(record));

        cleanupService.cleanupExpiredFiles();

        ArgumentCaptor<FileRecord> captor = ArgumentCaptor.forClass(FileRecord.class);
        verify(fileRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    private FileRecord buildRecord(String shareId, String storedFileName) {
        FileRecord r = new FileRecord();
        r.setShareId(shareId);
        r.setStoredFileName(storedFileName);
        r.setOriginalFileName("file.txt");
        r.setContentType("text/plain");
        r.setFileSize(100L);
        r.setStoragePath("uploads/" + storedFileName);
        r.setActive(true);
        r.setExpiresAt(Instant.now().minusSeconds(60));
        return r;
    }
}
