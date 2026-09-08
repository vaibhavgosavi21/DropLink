package com.example.fileshare.repository;

import com.example.fileshare.entity.FileRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class FileRepositoryTest {

    @Autowired
    private FileRepository fileRepository;

    @Test
    void savesAndFindsFileByShareId() {
        FileRecord record = buildRecord("share-abc", null);
        fileRepository.save(record);

        Optional<FileRecord> found = fileRepository.findByShareId("share-abc");
        assertThat(found).isPresent();
        assertThat(found.get().getOriginalFileName()).isEqualTo("test.txt");
    }

    @Test
    void findsExpiredActiveFiles() {
        Instant past = Instant.now().minusSeconds(3600);
        FileRecord expired = buildRecord("share-expired", past);
        fileRepository.save(expired);

        FileRecord notExpired = buildRecord("share-valid", Instant.now().plusSeconds(3600));
        fileRepository.save(notExpired);

        List<FileRecord> results = fileRepository.findAllByIsActiveAndExpiresAtBefore(true, Instant.now());
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getShareId()).isEqualTo("share-expired");
    }

    @Test
    void returnsEmptyForUnknownShareId() {
        Optional<FileRecord> result = fileRepository.findByShareId("nonexistent");
        assertThat(result).isEmpty();
    }

    private FileRecord buildRecord(String shareId, Instant expiresAt) {
        FileRecord r = new FileRecord();
        r.setOriginalFileName("test.txt");
        r.setStoredFileName("stored-" + shareId + ".txt");
        r.setContentType("text/plain");
        r.setFileSize(1024L);
        r.setStoragePath("uploads/stored-" + shareId + ".txt");
        r.setShareId(shareId);
        r.setExpiresAt(expiresAt);
        r.setActive(true);
        return r;
    }
}
