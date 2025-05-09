package org.example.cloudstorage.minio;

import java.util.List;

public interface MinioResourceSearchService {
    List<ObjectMetadata> search(String path, String query);
}
