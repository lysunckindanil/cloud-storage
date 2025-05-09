package org.example.cloudstorage.minio;

import java.util.List;

public interface MinioMetadataService {
    ObjectMetadata getResource(String path);

    List<ObjectMetadata> listFiles(String path, boolean recursive);
}
