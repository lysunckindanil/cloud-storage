package org.example.cloudstorage.minio;

import org.springframework.core.io.InputStreamResource;

public interface MinioResourceDownloadService {
    InputStreamResource download(String path);
}
