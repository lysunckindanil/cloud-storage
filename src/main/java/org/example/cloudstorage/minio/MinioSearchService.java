package org.example.cloudstorage.minio;

import org.example.cloudstorage.model.ResourceMetadata;

import java.util.List;

public interface MinioSearchService {
    List<ResourceMetadata> searchResources(String path, String query);
}
