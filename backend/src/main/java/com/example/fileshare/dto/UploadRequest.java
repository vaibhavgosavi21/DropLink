package com.example.fileshare.dto;

/**
 * Expiration options accepted from the client.
 * null / "never" means no expiration.
 */
public record UploadRequest(String expiresIn) {

    public static final String NEVER = "never";

    public boolean isNeverExpires() {
        return expiresIn == null || NEVER.equalsIgnoreCase(expiresIn);
    }
}
