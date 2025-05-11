package org.example.cloudstorage.minio;

import org.example.cloudstorage.model.ResourceMetadata;

import java.util.List;

public interface MinioMetadataService {
    ResourceMetadata getResource(String path);
    List<ResourceMetadata> listFiles(String path, boolean recursive);
}
