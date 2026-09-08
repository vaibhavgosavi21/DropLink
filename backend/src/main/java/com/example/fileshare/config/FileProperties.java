package com.example.fileshare.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileProperties {

    @Value("${file.upload.max-size}")
    private long maxSize;

    @Value("${file.storage.type}")
    private String storageType;

    @Value("${file.storage.local.upload-dir}")
    private String localUploadDir;

    @Value("${app.share.base-url}")
    private String shareBaseUrl;

    public long getMaxSize() { return maxSize; }
    public String getStorageType() { return storageType; }
    public String getLocalUploadDir() { return localUploadDir; }
    public String getShareBaseUrl() { return shareBaseUrl; }
}
