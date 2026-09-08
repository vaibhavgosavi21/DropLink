package com.example.fileshare.dto;

public record UploadedFileResponse(
    String fileName,
    long size,
    String contentType,
    String shareId,
    String shareUrl
) {}
