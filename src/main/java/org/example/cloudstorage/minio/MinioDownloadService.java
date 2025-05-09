package org.example.cloudstorage.minio;

import org.springframework.core.io.InputStreamResource;

public interface MinioDownloadService {
    InputStreamResource downloadResource(String path);
}
