package org.example.cloudstorage.minio;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
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
        path = PathUtils.normalizePathAsMinioKey(path);

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
        path = PathUtils.normalizePathAsMinioKey(path);

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
        path = PathUtils.normalizePathAsMinioKey(path);

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

    public void uploadObject(String path, MultipartFile file) {
        String uploadPath = PathUtils.normalizePathAsMinioKey(path + file.getOriginalFilename());
        if (!PathUtils.isPathValid(uploadPath))
            throw new InvalidPathMinioException("The upload path is invalid");

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
                throw new ResourceAlreadyExistsMinioException("File already exists: " + file.getOriginalFilename(), e);
            }
            throw new MinioException(e);
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    public void deleteObject(String path) {
        path = PathUtils.normalizePathAsMinioKey(path);
        try {
            String objectName = getObject(path).object();
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (ResourceNotFoundMinioException e) {
            throw new ResourceNotFoundMinioException("Resource not found", e);
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    public void createEmptyObject(String path) {
        path = PathUtils.normalizePathAsMinioKey(path);
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
        from = PathUtils.normalizePathAsMinioKey(from);
        to = PathUtils.normalizePathAsMinioKey(to);
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