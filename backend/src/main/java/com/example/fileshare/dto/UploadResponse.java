package com.example.fileshare.dto;

import java.util.List;

public record UploadResponse(
    boolean success,
    String message,
    UploadedFileResponse file,
    List<UploadedFileResponse> files
) {
    public static UploadResponse single(UploadedFileResponse file) {
        return new UploadResponse(true, "File uploaded successfully", file, null);
    }

    public static UploadResponse multiple(List<UploadedFileResponse> files) {
        return new UploadResponse(true, "Files uploaded successfully", null, files);
    }
}
