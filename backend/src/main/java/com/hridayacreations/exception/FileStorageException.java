package com.hridayacreations.exception;

/**
 * Thrown when an image/file storage operation (upload, delete, replace) fails, typically due to a
 * Cloudinary or I/O error. Mapped to HTTP 502.
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
