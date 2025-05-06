package org.example.cloudstorage.exception;

public class InvalidFilenameMinioException extends MinioException {
    public InvalidFilenameMinioException(String message) {
        super(message);
    }

    public InvalidFilenameMinioException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidFilenameMinioException(Throwable cause) {
        super(cause);
    }
}
