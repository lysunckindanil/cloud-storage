package org.example.cloudstorage.minio;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MinioManipulationService {
    void uploadResource(String path, List<MultipartFile> files);

    void deleteResource(String path);

    ObjectMetadata moveResource(String from, String to);

    void createEmptyDirectory(String path);
}
