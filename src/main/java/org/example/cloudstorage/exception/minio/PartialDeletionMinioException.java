package org.example.cloudstorage.exception.minio;

public class PartialDeletionMinioException extends MinioException {
    public PartialDeletionMinioException(String message) {
        super(message);
    }

    public PartialDeletionMinioException(String message, Throwable cause) {
        super(message, cause);
    }

    public PartialDeletionMinioException(Throwable cause) {
        super(cause);
    }
}
