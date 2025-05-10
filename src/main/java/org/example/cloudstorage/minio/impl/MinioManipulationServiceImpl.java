package org.example.cloudstorage.minio.impl;

import io.minio.messages.Item;
import org.example.cloudstorage.exception.minio.InvalidFileMinioException;
import org.example.cloudstorage.exception.minio.InvalidPathMinioException;
import org.example.cloudstorage.exception.minio.ResourceAlreadyExistsMinioException;
import org.example.cloudstorage.exception.minio.ResourceNotFoundMinioException;
import org.example.cloudstorage.minio.MinioManipulationService;
import org.example.cloudstorage.model.ObjectMetadata;
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
            createMissingDirectories(uploadedFiles, path);
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
    public ObjectMetadata moveResource(String from, String to) {
        if (from.equals(to))
            throw new InvalidPathMinioException("You cannot copy the object to the place where it currently locates");

        boolean fromIsDirectory = from.endsWith("/") || from.isEmpty();
        boolean toIsDirectory = to.endsWith("/") || to.isEmpty();

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
    public void createEmptyDirectory(String path, boolean ignoreExistence) {
        if (!path.endsWith("/") && !path.isEmpty())
            throw new InvalidPathMinioException("Directory path should end with slash");

        if (existsByPath(path) && !ignoreExistence)
            throw new ResourceAlreadyExistsMinioException("Directory already exists");

        for (String p : PathUtils.getNestedDirectories("", path)) {
            minioRepository.createEmptyObject(p + folderPostfix);
        }
        minioRepository.createEmptyObject(path + folderPostfix);
    }

    private ObjectMetadata moveFile(String from, String to) {
        if (existsByPath(to))
            throw new ResourceAlreadyExistsMinioException("Destination file already exists");
        if (!existsByPath(from))
            throw new ResourceNotFoundMinioException("Source file does not exist");

        minioRepository.copy(from, to);
        minioRepository.deleteObjects(List.of(from));
        createMissingDirectories(List.of(to), "");
        return new ObjectMetadata(to, false, minioRepository.getObject(to).size());
    }

    private ObjectMetadata moveDirectory(String from, String to) {
        if (!existsByPath(from))
            throw new ResourceNotFoundMinioException("Source directory does not exist");
        if (existsByPath(to))
            throw new ResourceAlreadyExistsMinioException("Destination directory already exists");

        List<String> copiedFiles = new ArrayList<>();
        List<Item> objects = minioRepository.getListObjects(from, true);

        try {
            for (Item item : objects) {
                String toPath = to + item.objectName().substring(from.length());
                minioRepository.copy(item.objectName(), toPath);
                copiedFiles.add(toPath);
            }
            createMissingDirectories(copiedFiles, "");
        } catch (Exception e) {
            rollbackCreatedObjects(copiedFiles, e);
            throw e;
        }
        minioRepository.deleteObjects(objects.stream().map(Item::objectName).toList());
        return new ObjectMetadata(to, true, 0L);
    }

    private boolean existsByPath(String path) {
        if (path.endsWith("/") || path.isEmpty()) {
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

    private void createMissingDirectories(List<String> paths, String basePath) {
        for (String path : paths) {
            for (String nestedDirectory : PathUtils.getNestedDirectories(basePath, path)) {
                createEmptyDirectory(nestedDirectory, true);
            }
        }
    }
}
