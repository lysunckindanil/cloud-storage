package org.example.cloudstorage.model;

public record ObjectMetadata(String name, boolean isDirectory, Long size) {
}
