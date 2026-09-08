package com.example.fileshare.controller;

import com.example.fileshare.dto.FileMetadataResponse;
import com.example.fileshare.dto.DownloadResult;
import com.example.fileshare.dto.UploadResponse;
import com.example.fileshare.exception.InvalidFileException;
import com.example.fileshare.service.FileService;
import com.example.fileshare.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final FileUploadService uploadService;
    private final FileService fileService;

    public FileController(FileUploadService uploadService, FileService fileService) {
        this.uploadService = uploadService;
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "expiresIn", defaultValue = "never") String expiresIn) {

        var uploaded = uploadService.upload(file, expiresIn);
        return ResponseEntity.status(HttpStatus.CREATED).body(UploadResponse.single(uploaded));
    }

    @PostMapping("/upload/multiple")
    public ResponseEntity<UploadResponse> uploadMultiple(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "expiresIn", defaultValue = "never") String expiresIn) {

        var uploaded = uploadService.uploadMultiple(files, expiresIn);
        return ResponseEntity.status(HttpStatus.CREATED).body(UploadResponse.multiple(uploaded));
    }

    @GetMapping("/share/{shareId}")
    public ResponseEntity<FileMetadataResponse> getMetadata(@PathVariable String shareId) {
        validateShareId(shareId);
        return ResponseEntity.ok(fileService.getFileMetadata(shareId));
    }

    @GetMapping("/share/{shareId}/download")
    public ResponseEntity<Resource> download(@PathVariable String shareId) {
        validateShareId(shareId);
        DownloadResult result = fileService.prepareDownload(shareId);

        String fileName = result.record().getOriginalFileName();
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        String contentDisposition = "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName;

        return ResponseEntity.ok()
            .contentType(resolveMediaType(result.record().getContentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
            .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(result.record().getFileSize()))
            .body(result.resource());
    }

    private MediaType resolveMediaType(String contentType) {
        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private void validateShareId(String shareId) {
        if (shareId == null || !shareId.matches("[A-Za-z0-9\\-_]{10,30}")) {
            throw new InvalidFileException("Invalid share ID format.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}
