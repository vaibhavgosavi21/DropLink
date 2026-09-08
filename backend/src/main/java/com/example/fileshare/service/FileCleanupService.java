package com.example.fileshare.service;

import com.example.fileshare.entity.FileRecord;
import com.example.fileshare.repository.FileRepository;
import com.example.fileshare.service.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class FileCleanupService {

    private static final Logger log = LoggerFactory.getLogger(FileCleanupService.class);

    private final FileRepository fileRepository;
    private final FileStorageService storageService;

    public FileCleanupService(FileRepository fileRepository, FileStorageService storageService) {
        this.fileRepository = fileRepository;
        this.storageService = storageService;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public int cleanupExpiredFiles() {
        List<FileRecord> expired = fileRepository.findAllByIsActiveAndExpiresAtBefore(true, Instant.now());
        for (FileRecord record : expired) {
            record.setActive(false);
            storageService.delete(record.getStoredFileName());
            fileRepository.save(record);
            log.info("Expired file cleaned up: shareId={}", record.getShareId());
        }
        if (!expired.isEmpty()) {
            log.info("Cleanup complete. Removed {} expired file(s).", expired.size());
        }
        return expired.size();
    }
}
