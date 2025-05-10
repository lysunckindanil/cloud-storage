package org.example.cloudstorage.minio.impl;

import io.minio.StatObjectResponse;
import org.example.cloudstorage.exception.minio.ResourceNotFoundMinioException;
import org.example.cloudstorage.minio.MinioMetadataService;
import org.example.cloudstorage.model.ObjectMetadata;

import java.util.List;

public class MinioMetadataServiceImpl implements MinioMetadataService {
    private final MinioRepository minioRepository;
    private final String folderPostfix;

    public MinioMetadataServiceImpl(MinioRepository minioRepository, String folderPostfix) {
        this.minioRepository = minioRepository;
        this.folderPostfix = folderPostfix;
    }

    @Override
    public ObjectMetadata getResource(String path) {
        boolean isDir = isDir(path);
        StatObjectResponse statObject = minioRepository.getObject(
                isDir ? path + folderPostfix : path
        );
        return new ObjectMetadata(
                isDir ? path : statObject.object(),
                isDir,
                statObject.size()
        );
    }

    @Override
    public List<ObjectMetadata> listFiles(String path, boolean recursive) {
        if (!existsByPath(path)) throw new ResourceNotFoundMinioException("Folder is not found");
        return minioRepository.getListObjects(path, recursive)
                .stream()
                .filter(item -> !item.objectName().endsWith(folderPostfix))
                .map(item -> new ObjectMetadata(item.objectName(), item.isDir(), item.size()))
                .toList();
    }

    private boolean existsByPath(String path) {
        if (isDir(path)) {
            return !minioRepository.getListObjects(path + folderPostfix, false).isEmpty();
        }
        return !minioRepository.getListObjects(path, false).isEmpty();
    }

    private boolean isDir(String path) {
        return path.endsWith("/") || path.isEmpty();
    }
}
