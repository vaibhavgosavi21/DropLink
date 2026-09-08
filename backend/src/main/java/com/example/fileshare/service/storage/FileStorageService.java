package com.example.fileshare.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String store(MultipartFile file, String storedFileName);
    Resource load(String storedFileName);
    void delete(String storedFileName);
    boolean exists(String storedFileName);
}
