package org.example.cloudstorage.minio;

import org.example.cloudstorage.model.ResourceMetadata;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MinioManipulationService {
    void uploadResources(String path, List<MultipartFile> files);

    void deleteResource(String path);

    ResourceMetadata moveResource(String from, String to);

    void createEmptyDirectory(String path, boolean ignoreExistence);
}
