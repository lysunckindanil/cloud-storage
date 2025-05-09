package org.example.cloudstorage.minio.impl;

import io.minio.StatObjectResponse;
import org.example.cloudstorage.exception.minio.ResourceNotFoundMinioException;
import org.example.cloudstorage.minio.MinioRepository;
import org.example.cloudstorage.minio.MinioResourceMetadataService;
import org.example.cloudstorage.minio.ObjectMetadata;

import java.util.List;

public class MinioResourceMetadataServiceImpl implements MinioResourceMetadataService {
    private final MinioRepository minioRepository;
    private final String folderPostfix;

    MinioResourceMetadataServiceImpl(MinioRepository minioRepository, String folderPostfix) {
        this.minioRepository = minioRepository;
        this.folderPostfix = folderPostfix;
    }

    @Override
    public ObjectMetadata get(String path) {
        boolean isDir = path.endsWith("/");
        StatObjectResponse statObject = minioRepository.getObject(
                isDir ? path + folderPostfix : path
        );
        return new ObjectMetadata(
                statObject.object(),
                isDir,
                statObject.size()
        );
    }

    @Override
    public List<ObjectMetadata> list(String path, boolean recursive) {
        if (!existsByPath(path)) throw new ResourceNotFoundMinioException("Folder is not found");
        return minioRepository.getListObjects(path, recursive)
                .stream()
                .filter(item -> !item.objectName().endsWith(folderPostfix))
                .map(item -> new ObjectMetadata(item.objectName(), item.isDir(), item.size()))
                .toList();
    }

    private boolean existsByPath(String path) {
        if (path.endsWith("/")) {
            path = path + folderPostfix;
        }
        try {
            minioRepository.getObject(path);
        } catch (ResourceNotFoundMinioException e) {
            return false;
        }
        return true;
    }
}
