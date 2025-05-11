package org.example.cloudstorage.minio.impl;

import io.minio.StatObjectResponse;
import org.example.cloudstorage.exception.minio.InvalidPathMinioException;
import org.example.cloudstorage.exception.minio.ResourceNotFoundMinioException;
import org.example.cloudstorage.minio.MinioMetadataService;
import org.example.cloudstorage.model.ResourceMetadata;

import java.util.List;

public class MinioMetadataServiceImpl implements MinioMetadataService {
    private final MinioRepository minioRepository;
    private final String folderPostfix;

    public MinioMetadataServiceImpl(MinioRepository minioRepository, String folderPostfix) {
        this.minioRepository = minioRepository;
        this.folderPostfix = folderPostfix;
    }

    @Override
    public ResourceMetadata getResource(String path) {
        boolean isDir = isDir(path);
        StatObjectResponse statObject = minioRepository.getObject(
                isDir ? path + folderPostfix : path
        );
        return new ResourceMetadata(
                isDir ? path : statObject.object(),
                isDir,
                statObject.size()
        );
    }

    @Override
    public List<ResourceMetadata> listFiles(String path, boolean recursive) {
        if (!isDir(path)) throw new InvalidPathMinioException("Path must be a directory");
        if (!existsByPath(path)) throw new ResourceNotFoundMinioException("Directory is not found");
        return minioRepository.getListObjects(path, recursive)
                .stream()
                .filter(item -> !item.objectName().endsWith(folderPostfix))
                .map(item -> new ResourceMetadata(item.objectName(), item.isDir(), item.size()))
                .toList();
    }

    private boolean existsByPath(String path) {
        if (isDir(path)) {
            return !minioRepository.getListObjects(path + folderPostfix, false).isEmpty();
        }
        return !minioRepository.getListObjects(path, false).isEmpty();
    }

    private boolean isDir(String path) {
        return path.endsWith("/");
    }
}
