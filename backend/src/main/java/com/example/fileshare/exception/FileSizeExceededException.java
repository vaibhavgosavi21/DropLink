package com.example.fileshare.exception;

public class FileSizeExceededException extends RuntimeException {
    public FileSizeExceededException(String message) { super(message); }
}
