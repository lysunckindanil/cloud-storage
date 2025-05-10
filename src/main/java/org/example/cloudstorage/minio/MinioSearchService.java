package org.example.cloudstorage.minio;

import org.example.cloudstorage.model.ObjectMetadata;

import java.util.List;

public interface MinioSearchService {
    List<ObjectMetadata> searchResources(String path, String query);
}
