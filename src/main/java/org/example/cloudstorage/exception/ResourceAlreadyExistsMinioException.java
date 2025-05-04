package org.example.cloudstorage.exception;

public class ResourceAlreadyExistsMinioException extends MinioException {
    public ResourceAlreadyExistsMinioException(String message) {
        super(message);
    }

    public ResourceAlreadyExistsMinioException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceAlreadyExistsMinioException(Throwable cause) {
        super(cause);
    }
}
