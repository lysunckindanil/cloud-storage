package org.example.cloudstorage.exception;

public class InvalidPathMinioException extends MinioException {
    public InvalidPathMinioException(String message) {
        super(message);
    }

    public InvalidPathMinioException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPathMinioException(Throwable cause) {
        super(cause);
    }
}
