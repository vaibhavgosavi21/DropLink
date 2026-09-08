package com.example.fileshare.service;

import com.example.fileshare.config.FileProperties;
import com.example.fileshare.dto.UploadedFileResponse;
import com.example.fileshare.entity.FileRecord;
import com.example.fileshare.repository.FileRepository;
import com.example.fileshare.service.storage.FileStorageService;
import com.example.fileshare.exception.InvalidFileException;
import com.example.fileshare.util.ExpirationParser;
import com.example.fileshare.util.FileMapper;
import com.example.fileshare.util.ShareIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadService.class);

    private final FileRepository fileRepository;
    private final FileStorageService storageService;
    private final FileValidationService validationService;
    private final FileProperties fileProperties;

    public FileUploadService(FileRepository fileRepository,
                             FileStorageService storageService,
                             FileValidationService validationService,
                             FileProperties fileProperties) {
        this.fileRepository = fileRepository;
        this.storageService = storageService;
        this.validationService = validationService;
        this.fileProperties = fileProperties;
    }

    public UploadedFileResponse upload(MultipartFile file, String expiresIn) {
        log.info("Upload started: originalName={}, size={}", file.getOriginalFilename(), file.getSize());

        validationService.validate(file);

        String sanitizedName = validationService.sanitizeFileName(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + "_" + sanitizedName;
        String shareId = ShareIdGenerator.generate();

        storageService.store(file, storedFileName);

        FileRecord record = buildRecord(file, sanitizedName, storedFileName, shareId, expiresIn);
        fileRepository.save(record);

        log.info("Upload completed: shareId={}, storedFileName={}", shareId, storedFileName);
        return FileMapper.toUploadedFileResponse(record, fileProperties.getShareBaseUrl());
    }

    public List<UploadedFileResponse> uploadMultiple(List<MultipartFile> files, String expiresIn) {
        return files.stream()
            .map(file -> upload(file, expiresIn))
            .toList();
    }

    private FileRecord buildRecord(MultipartFile file, String sanitizedName,
                                   String storedFileName, String shareId, String expiresIn) {
        FileRecord record = new FileRecord();
        record.setOriginalFileName(sanitizedName);
        record.setStoredFileName(storedFileName);
        record.setContentType(resolveContentType(file));
        record.setFileSize(file.getSize());
        record.setStoragePath(fileProperties.getLocalUploadDir() + "/" + storedFileName);
        record.setShareId(shareId);
        record.setExpiresAt(parseExpiration(expiresIn));
        record.setActive(true);
        return record;
    }

    private java.time.Instant parseExpiration(String expiresIn) {
        try {
            return ExpirationParser.parse(expiresIn);
        } catch (IllegalArgumentException e) {
            throw new InvalidFileException(e.getMessage());
        }
    }

    private String resolveContentType(MultipartFile file) {
        String ct = file.getContentType();
        return (ct != null && !ct.isBlank()) ? ct : "application/octet-stream";
    }
}
