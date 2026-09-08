package com.example.fileshare.dto;

import java.time.Instant;

public record FileMetadataResponse(
    String fileName,
    String contentType,
    long fileSize,
    String shareId,
    Instant createdAt,
    Instant expiresAt,
    long downloadCount
) {}
