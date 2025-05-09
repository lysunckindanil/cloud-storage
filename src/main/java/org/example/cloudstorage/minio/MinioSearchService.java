package org.example.cloudstorage.minio;

import java.util.List;

public interface MinioSearchService {
    List<ObjectMetadata> searchResources(String path, String query);
}
