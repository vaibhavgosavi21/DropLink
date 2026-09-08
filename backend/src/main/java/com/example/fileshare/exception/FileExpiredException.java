package com.example.fileshare.exception;

public class FileExpiredException extends RuntimeException {
    public FileExpiredException(String message) { super(message); }
}
