package org.example.cloudstorage.minio;

import java.util.List;

public interface MinioResourceMetadataService {
    ObjectMetadata get(String path);

    List<ObjectMetadata> list(String path, boolean recursive);
}
