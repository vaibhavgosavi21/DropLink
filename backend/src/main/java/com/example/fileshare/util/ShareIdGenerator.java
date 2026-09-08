package com.example.fileshare.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class ShareIdGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int BYTE_LENGTH = 16;

    private ShareIdGenerator() {}

    public static String generate() {
        byte[] bytes = new byte[BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
