package com.example.fileshare.service;

import com.example.fileshare.config.FileProperties;
import com.example.fileshare.exception.FileSizeExceededException;
import com.example.fileshare.exception.InvalidFileException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
public class FileValidationService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "webp", "bmp",
        "mp4", "mov", "avi", "mkv", "webm",
        "pdf", "doc", "docx", "xls", "xlsx",
        "zip", "tar", "gz", "rar",
        "txt", "csv", "json", "xml", "md"
    );

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
        "exe", "bat", "cmd", "sh", "ps1", "msi",
        "dll", "so", "dylib", "jar", "war",
        "php", "py", "rb", "js", "ts", "jsp"
    );

    private final FileProperties fileProperties;

    public FileValidationService(FileProperties fileProperties) {
        this.fileProperties = fileProperties;
    }

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("No file provided or file is empty.");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new InvalidFileException("File name is missing.");
        }

        String extension = extractExtension(originalName);
        if (BLOCKED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileException("File type not permitted: ." + extension);
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileException("Unsupported file type: ." + extension);
        }

        if (file.getSize() > fileProperties.getMaxSize()) {
            throw new FileSizeExceededException(
                "File size exceeds the maximum allowed limit of " + (fileProperties.getMaxSize() / 1024 / 1024) + " MB."
            );
        }
    }

    public String sanitizeFileName(String originalFileName) {
        if (originalFileName == null) return "file";
        // Strip path components, keep only the filename
        String name = originalFileName
            .replaceAll(".*[/\\\\]", "")   // remove any path prefix
            .replaceAll("[^a-zA-Z0-9._\\-]", "_") // replace unsafe chars
            .replaceAll("\\.{2,}", ".");    // collapse multiple dots
        return name.isBlank() ? "file" : name;
    }

    private String extractExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            throw new InvalidFileException("File has no extension.");
        }
        return fileName.substring(dot + 1).toLowerCase();
    }
}
