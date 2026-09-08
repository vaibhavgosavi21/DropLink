package com.example.fileshare.util;

import com.example.fileshare.dto.FileMetadataResponse;
import com.example.fileshare.dto.UploadedFileResponse;
import com.example.fileshare.entity.FileRecord;

public final class FileMapper {

    private FileMapper() {}

    public static FileMetadataResponse toMetadataResponse(FileRecord record) {
        return new FileMetadataResponse(
            record.getOriginalFileName(),
            record.getContentType(),
            record.getFileSize(),
            record.getShareId(),
            record.getCreatedAt(),
            record.getExpiresAt(),
            record.getDownloadCount()
        );
    }

    public static UploadedFileResponse toUploadedFileResponse(FileRecord record, String shareBaseUrl) {
        return new UploadedFileResponse(
            record.getOriginalFileName(),
            record.getFileSize(),
            record.getContentType(),
            record.getShareId(),
            shareBaseUrl + "/share/" + record.getShareId()
        );
    }
}
