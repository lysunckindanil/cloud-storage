package org.example.cloudstorage.minio;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MinioResourceManipulationService {
    void upload(String path, List<MultipartFile> files);

    void delete(String path);

    ObjectMetadata move(String from, String to);
}
