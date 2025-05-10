package org.example.cloudstorage.minio.impl;

import io.minio.StatObjectResponse;
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
        } catch (Exception e) {
            for (String fileName : uploadedFiles) {
                try {
                    minioRepository.deleteObjects(List.of(fileName));
                } catch (Exception deleteEx) {
                    e.addSuppressed(deleteEx);
                }
            }
            throw e;
        }

        for (String fileName : uploadedFiles) {
            for (String nestedDirectory : PathUtils.getNestedDirectories(path, fileName)) {
                createEmptyDirectory(nestedDirectory, true);
            }
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
        }

        if (!fromIsDirectory && !toIsDirectory) {
            return moveFile(from, to);
        }

        if (fromIsDirectory) {
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

        for (String nestedDirectory : PathUtils.getNestedDirectories("", to)) {
            createEmptyDirectory(nestedDirectory, true);
        }
        StatObjectResponse statObjectResponse = minioRepository.getObject(from);
        minioRepository.copy(statObjectResponse.object(), to);
        minioRepository.deleteObjects(List.of(statObjectResponse.object()));
        return new ObjectMetadata(to, false, statObjectResponse.size());
    }

    private ObjectMetadata moveDirectory(String from, String to) {
        if (!existsByPath(from))
            throw new ResourceNotFoundMinioException("Source directory does not exist");
        if (existsByPath(to))
            throw new ResourceAlreadyExistsMinioException("Destination directory already exists");

        List<String> copiedFilesFrom = new ArrayList<>();
        List<String> copiedFilesTo = new ArrayList<>();

        List<Item> objects = minioRepository.getListObjects(from, true);
        try {
            for (Item item : objects) {
                String toPath = to + item.objectName().substring(from.length());
                minioRepository.copy(item.objectName(), toPath);
                copiedFilesFrom.add(item.objectName());
                copiedFilesTo.add(toPath);
            }
        } catch (Exception e) {
            minioRepository.deleteObjects(copiedFilesTo);
            throw e;
        }

        minioRepository.deleteObjects(copiedFilesFrom);

        for (String fileTo : copiedFilesTo) {
            for (String nestedDirectory : PathUtils.getNestedDirectories("", fileTo)) {
                createEmptyDirectory(nestedDirectory, true);
            }
        }

        if (from.isEmpty()) {
            createEmptyDirectory(from, true);
        }

        return new ObjectMetadata(to, true, 0L);
    }

    private boolean existsByPath(String path) {
        if (path.endsWith("/") || path.isEmpty()) {
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
