package org.example.cloudstorage.minio.impl;

import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import org.example.cloudstorage.exception.minio.*;
import org.example.cloudstorage.minio.MinioManipulationService;
import org.example.cloudstorage.minio.MinioRepository;
import org.example.cloudstorage.minio.ObjectMetadata;
import org.example.cloudstorage.util.PathUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class MinioManipulationServiceImpl implements MinioManipulationService {

    private final MinioRepository minioRepository;
    private final String folderPostfix;

    public MinioManipulationServiceImpl(MinioRepository minioRepository, String folderPostfix) {
        this.minioRepository = minioRepository;
        this.folderPostfix = folderPostfix;
    }

    @Override
    public void uploadResource(String path, List<MultipartFile> files) {
        List<String> uploadedFiles = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                if (file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty())
                    throw new InvalidFileMinioException("File name should not be empty");
                uploadedFiles.add(minioRepository.uploadObject(path, file, file.getOriginalFilename()));
            }
        } catch (Exception e) {
            for (String fileName : uploadedFiles) {
                minioRepository.deleteObject(fileName);
            }
            throw e;
        }

        for (String fileName : uploadedFiles) {
            for (String nestedDirectory : PathUtils.getNestedDirectories(path, fileName)) {
                createEmptyDirectory(nestedDirectory);
            }
        }
    }

    @Override
    public void deleteResource(String path) {
        try {
            for (Item item : minioRepository.getListObjects(path, true)) {
                minioRepository.deleteObject(item.objectName());
            }
        } catch (MinioException e) {
            throw new PartialDeletionMinioException(e);
        }
    }

    @Override
    public ObjectMetadata moveResource(String from, String to) {
        if (!from.endsWith("/")) {
            return moveFile(from, to);
        } else {
            return moveDirectory(from, to);
        }
    }

    @Override
    public void createEmptyDirectory(String path) {
        minioRepository.createEmptyObject(path + folderPostfix);
    }

    private ObjectMetadata moveFile(String from, String to) {
        if (existsByPath(to))
            throw new ResourceAlreadyExistsMinioException("Have already file with the same name");
        for (String nestedDirectory : PathUtils.getNestedDirectories("", to)) {
            createEmptyDirectory(nestedDirectory + folderPostfix);
        }
        StatObjectResponse statObjectResponse = minioRepository.getObject(from);
        minioRepository.copy(statObjectResponse.object(), to);
        minioRepository.deleteObject(statObjectResponse.object());
        return new ObjectMetadata(to, false, statObjectResponse.size());
    }

    private ObjectMetadata moveDirectory(String from, String to) {
        List<String> copiedFilesFrom = new ArrayList<>();
        List<String> copiedFilesTo = new ArrayList<>();
        List<Item> objects = minioRepository.getListObjects(from, true);

        try {
            for (Item item : objects) {
                String toPath = to + item.objectName().substring(from.length());
                if (existsByPath(toPath)) {
                    throw new ResourceAlreadyExistsMinioException("Destination already exists");
                }
                minioRepository.copy(item.objectName(), toPath);
                copiedFilesFrom.add(item.objectName());
                copiedFilesTo.add(toPath);
            }
        } catch (Exception e) {
            for (String fileName : copiedFilesTo) {
                minioRepository.deleteObject(fileName);
            }
            throw e;
        }

        for (String fileFrom : copiedFilesFrom) {
            minioRepository.deleteObject(fileFrom);
        }

        for (String fileTo : copiedFilesTo) {
            for (String nestedDirectory : PathUtils.getNestedDirectories("", fileTo)) {
                createEmptyDirectory(nestedDirectory);
            }
        }

        return new ObjectMetadata(to, true, 0L);
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
