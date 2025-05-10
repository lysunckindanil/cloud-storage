package org.example.cloudstorage.minio.impl;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.exception.minio.InvalidPathMinioException;
import org.example.cloudstorage.exception.minio.MinioException;
import org.example.cloudstorage.exception.minio.ResourceAlreadyExistsMinioException;
import org.example.cloudstorage.exception.minio.ResourceNotFoundMinioException;
import org.example.cloudstorage.util.PathUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
public class MinioRepository {

    private final MinioClient minioClient;
    private final String bucketName;

    public StatObjectResponse getObject(String path) {
        path = PathUtils.normalizePathMinioCompatible(path);

        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build());
        } catch (ErrorResponseException e) {
            throw new ResourceNotFoundMinioException("Resource not found", e);
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    public List<Item> getListObjects(String path, boolean recursive) {
        path = PathUtils.normalizePathMinioCompatible(path);

        var result = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(path)
                        .recursive(recursive)
                        .build()
        );

        List<Item> items = new ArrayList<>();
        try {
            for (Result<Item> item : result) {
                items.add(item.get());
            }
        } catch (Exception e) {
            throw new MinioException("Error occurred while fetching list of objects", e);
        }
        return items;
    }

    public InputStream downloadObject(String path) {
        path = PathUtils.normalizePathMinioCompatible(path);

        try {
            return minioClient.getObject(GetObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .object(path)
                    .build());
        } catch (ErrorResponseException e) {
            throw new ResourceNotFoundMinioException("Resource not found", e);
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    public String uploadObject(String path, MultipartFile file, String fileName) {
        String uploadPath = PathUtils.normalizePathMinioCompatible(path + fileName);

        Map<String, String> headers = new HashMap<>();
        headers.put("If-None-Match", "*");
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uploadPath)
                            .headers(headers)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("PreconditionFailed")) {
                throw new ResourceAlreadyExistsMinioException("File already exists: " + fileName, e);
            }
            throw new MinioException(e);
        } catch (Exception e) {
            throw new MinioException(e);
        }

        return uploadPath;
    }

    public void deleteObjects(List<String> objects) {
        List<DeleteObject> forDelete = objects.stream()
                .map(PathUtils::normalizePathMinioCompatible)
                .map(DeleteObject::new)
                .toList();

        Iterable<Result<DeleteError>> results =
                minioClient.removeObjects(
                        RemoveObjectsArgs.builder()
                                .bucket(bucketName)
                                .objects(forDelete)
                                .build());

        for (Result<DeleteError> result : results) {
            try {
                result.get();
            } catch (Exception e) {
                throw new MinioException(e);
            }
        }
    }

    public void createEmptyObject(String path) {
        path = PathUtils.normalizePathMinioCompatible(path);

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build());
        } catch (ErrorResponseException e) {
            throw new ResourceNotFoundMinioException("Resource not found", e);
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    public void copy(String from, String to) {
        from = PathUtils.normalizePathMinioCompatible(from);
        to = PathUtils.normalizePathMinioCompatible(to);

        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucketName)
                    .object(to)
                    .source(CopySource.builder()
                            .bucket(bucketName)
                            .object(from)
                            .build())
                    .build());
        } catch (ErrorResponseException e) {
            throw new InvalidPathMinioException("You cannot copy the object to the place where it currently locates");
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }
}