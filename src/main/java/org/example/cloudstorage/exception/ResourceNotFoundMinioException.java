package org.example.cloudstorage.exception;

public class ResourceNotFoundMinioException extends MinioException {
    public ResourceNotFoundMinioException(String message) {
        super(message);
    }

    public ResourceNotFoundMinioException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceNotFoundMinioException(Throwable cause) {
        super(cause);
    }
}
