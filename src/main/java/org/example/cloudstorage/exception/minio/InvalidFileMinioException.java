package org.example.cloudstorage.exception.minio;

public class InvalidFileMinioException extends MinioException {
    public InvalidFileMinioException(Throwable cause) {
        super(cause);
    }

    public InvalidFileMinioException(String message) {
        super(message);
    }

    public InvalidFileMinioException(String message, Throwable cause) {
        super(message, cause);
    }
}
