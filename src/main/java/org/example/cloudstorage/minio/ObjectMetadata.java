package org.example.cloudstorage.minio;

public record ObjectMetadata(String name, boolean isDirectory, Long size) {
}
