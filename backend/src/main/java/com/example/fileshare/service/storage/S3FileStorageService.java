package com.example.fileshare.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * S3 storage implementation — to be activated with file.storage.type=s3 in production.
 * Requires AWS SDK dependency and proper credentials configuration.
 */
public class S3FileStorageService implements FileStorageService {

    @Override
    public String store(MultipartFile file, String storedFileName) {
        throw new UnsupportedOperationException("S3 storage not yet configured. Set file.storage.type=local for development.");
    }

    @Override
    public Resource load(String storedFileName) {
        throw new UnsupportedOperationException("S3 storage not yet configured.");
    }

    @Override
    public void delete(String storedFileName) {
        throw new UnsupportedOperationException("S3 storage not yet configured.");
    }

    @Override
    public boolean exists(String storedFileName) {
        throw new UnsupportedOperationException("S3 storage not yet configured.");
    }
}
