package com.example.fileshare.service;

import com.example.fileshare.config.FileProperties;
import com.example.fileshare.dto.FileMetadataResponse;
import com.example.fileshare.dto.UploadedFileResponse;
import com.example.fileshare.dto.DownloadResult;
import com.example.fileshare.entity.FileRecord;
import com.example.fileshare.exception.FileExpiredException;
import com.example.fileshare.exception.FileNotFoundException;
import com.example.fileshare.repository.FileRepository;
import com.example.fileshare.service.storage.FileStorageService;
import com.example.fileshare.util.FileMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final FileRepository fileRepository;
    private final FileStorageService storageService;
    private final FileProperties fileProperties;

    public FileService(FileRepository fileRepository,
                       FileStorageService storageService,
                       FileProperties fileProperties) {
        this.fileRepository = fileRepository;
        this.storageService = storageService;
        this.fileProperties = fileProperties;
    }

    @Transactional(readOnly = true)
    public FileMetadataResponse getFileMetadata(String shareId) {
        FileRecord record = findActiveRecord(shareId);
        return FileMapper.toMetadataResponse(record);
    }

    public DownloadResult prepareDownload(String shareId) {
        FileRecord record = findActiveRecord(shareId);
        record.setDownloadCount(record.getDownloadCount() + 1);
        fileRepository.save(record);
        Resource resource = storageService.load(record.getStoredFileName());
        log.info("Download prepared: shareId={}, downloadCount={}", shareId, record.getDownloadCount());
        return new DownloadResult(record, resource);
    }

    public void deleteFile(UUID id) {
        FileRecord record = fileRepository.findById(id)
            .orElseThrow(() -> new FileNotFoundException("File not found: " + id));
        record.setActive(false);
        storageService.delete(record.getStoredFileName());
        fileRepository.save(record);
        log.info("File deleted: {}", record.getShareId());
    }

    private FileRecord findActiveRecord(String shareId) {
        FileRecord record = fileRepository.findByShareId(shareId)
            .orElseThrow(() -> new FileNotFoundException("File not found for shareId: " + shareId));

        if (!record.isActive()) {
            throw new FileNotFoundException("File is no longer available.");
        }

        if (record.getExpiresAt() != null && Instant.now().isAfter(record.getExpiresAt())) {
            log.info("Expired file accessed: {}", shareId);
            throw new FileExpiredException("This link has expired.");
        }

        return record;
    }
}
