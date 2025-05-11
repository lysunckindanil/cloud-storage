package org.example.cloudstorage.minio.impl;

import io.minio.messages.Item;
import org.example.cloudstorage.exception.minio.InvalidFileMinioException;
import org.example.cloudstorage.exception.minio.InvalidPathMinioException;
import org.example.cloudstorage.exception.minio.ResourceAlreadyExistsMinioException;
import org.example.cloudstorage.exception.minio.ResourceNotFoundMinioException;
import org.example.cloudstorage.minio.MinioManipulationService;
import org.example.cloudstorage.model.ResourceMetadata;
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
    public void uploadResources(String path, List<MultipartFile> files) {
        List<String> uploadedFiles = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                if (file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty())
                    throw new InvalidFileMinioException("File name should not be empty");

                uploadedFiles.add(minioRepository.uploadObject(path, file, file.getOriginalFilename()));
            }
            createMissingDirectories(uploadedFiles);
        } catch (Exception e) {
            rollbackCreatedObjects(uploadedFiles, e);
            throw e;
        }
    }

    @Override
    public void deleteResource(String path) {
        if (!existsByPath(path))
            throw new ResourceNotFoundMinioException("Resource does not exist");

        minioRepository.deleteObjects(
                minioRepository.getListObjects(path, true)
                        .stream()
                        .map(Item::objectName)
                        .toList()
        );
    }

    @Override
    public ResourceMetadata moveResource(String from, String to) {
        if (from.equals(to))
            throw new InvalidPathMinioException("You cannot copy the object to the place where it currently locates");

        boolean fromIsDirectory = isDir(from);
        boolean toIsDirectory = isDir(to);

        from = PathUtils.normalizePathMinioCompatible(from);
        to = PathUtils.normalizePathMinioCompatible(to);

        if (fromIsDirectory && toIsDirectory) {
            return moveDirectory(from, to);
        } else if (!fromIsDirectory && !toIsDirectory) {
            return moveFile(from, to);
        } else if (fromIsDirectory) {
            throw new InvalidPathMinioException("You cannot move a directory to a file");
        }
        throw new InvalidPathMinioException("You cannot turn a file into a directory");
    }

    @Override
    public ResourceMetadata createEmptyDirectory(String path, boolean ignoreExistence) {
        if (!isDir(path))
            throw new InvalidPathMinioException("Path should be a directory");

        if (existsByPath(path) && !ignoreExistence)
            throw new ResourceAlreadyExistsMinioException("Directory already exists");

        for (String p : PathUtils.getNestedDirectories("", path)) {
            minioRepository.createEmptyObject(p + folderPostfix);
        }
        minioRepository.createEmptyObject(path + folderPostfix);
        return new ResourceMetadata(path, true, 0L);
    }

    private ResourceMetadata moveFile(String from, String to) {
        if (existsByPath(to))
            throw new ResourceAlreadyExistsMinioException("Destination file already exists");
        if (!existsByPath(from))
            throw new ResourceNotFoundMinioException("Source file does not exist");

        minioRepository.copy(from, to);
        minioRepository.deleteObjects(List.of(from));
        createMissingDirectories(List.of(to));
        return new ResourceMetadata(to, false, minioRepository.getObject(to).size());
    }

    private ResourceMetadata moveDirectory(String from, String to) {
        if (!existsByPath(from))
            throw new ResourceNotFoundMinioException("Source directory does not exist");
        if (existsByPath(to))
            throw new ResourceAlreadyExistsMinioException("Destination directory already exists");

        List<String> copiedFiles = new ArrayList<>();
        List<String> objects = minioRepository.getListObjects(from, true)
                .stream().map(Item::objectName)
                .toList();
        try {
            for (String objectName : objects) {
                String toPath = to + objectName.substring(from.length());
                minioRepository.copy(objectName, toPath);
                copiedFiles.add(toPath);
            }
        } catch (Exception e) {
            rollbackCreatedObjects(copiedFiles, e);
            throw e;
        }
        minioRepository.deleteObjects(objects);
        return new ResourceMetadata(to, true, 0L);
    }

    private boolean existsByPath(String path) {
        if (isDir(path)) {
            return !minioRepository.getListObjects(path + folderPostfix, false).isEmpty();
        }
        return !minioRepository.getListObjects(path, false).isEmpty();
    }

    private void rollbackCreatedObjects(List<String> createdObjects, Exception e) {
        try {
            minioRepository.deleteObjects(createdObjects);
        } catch (Exception deleteEx) {
            e.addSuppressed(deleteEx);
        }
    }

    private void createMissingDirectories(List<String> paths) {
        for (String path : paths) {
            for (String nestedDirectory : PathUtils.getNestedDirectories("", path)) {
                createEmptyDirectory(nestedDirectory, true);
            }
        }
    }

    private boolean isDir(String path) {
        return path.endsWith("/");
    }
}
