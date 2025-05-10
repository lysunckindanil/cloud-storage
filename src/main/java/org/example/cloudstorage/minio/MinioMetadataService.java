package org.example.cloudstorage.minio;

import org.example.cloudstorage.model.ObjectMetadata;

import java.util.List;

public interface MinioMetadataService {
    ObjectMetadata getResource(String path);

    List<ObjectMetadata> listFiles(String path, boolean recursive);
}
