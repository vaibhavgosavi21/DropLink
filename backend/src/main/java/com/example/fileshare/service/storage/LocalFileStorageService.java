package com.example.fileshare.service.storage;

import com.example.fileshare.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);

    @Value("${file.storage.local.upload-dir}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
            log.info("Local storage initialized at: {}", uploadPath);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create upload directory", e);
        }
    }

    @Override
    public String store(MultipartFile file, String storedFileName) {
        Path target = resolveSecure(storedFileName);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Stored file: {}", storedFileName);
            return storedFileName;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file: " + storedFileName, e);
        }
    }

    @Override
    public Resource load(String storedFileName) {
        Path file = resolveSecure(storedFileName);
        try {
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new FileStorageException("File not readable: " + storedFileName);
        } catch (MalformedURLException e) {
            throw new FileStorageException("Malformed file path: " + storedFileName, e);
        }
    }

    @Override
    public void delete(String storedFileName) {
        Path file = resolveSecure(storedFileName);
        try {
            Files.deleteIfExists(file);
            log.debug("Deleted file: {}", storedFileName);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete file: " + storedFileName, e);
        }
    }

    @Override
    public boolean exists(String storedFileName) {
        return Files.exists(resolveSecure(storedFileName));
    }

    private Path resolveSecure(String fileName) {
        Path resolved = uploadPath.resolve(fileName).normalize();
        if (!resolved.startsWith(uploadPath)) {
            throw new FileStorageException("Path traversal attempt detected: " + fileName);
        }
        return resolved;
    }
}
