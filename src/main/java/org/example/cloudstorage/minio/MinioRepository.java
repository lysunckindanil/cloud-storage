package org.example.cloudstorage.minio;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.exception.InvalidFilenameMinioException;
import org.example.cloudstorage.exception.MinioException;
import org.example.cloudstorage.exception.ResourceNotFoundMinioException;
import org.example.cloudstorage.util.PathUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


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
            throw new ResourceNotFoundMinioException(e);
        } catch (Exception e) {
            throw new MinioException(e);
        }
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
            throw new ResourceNotFoundMinioException(e);
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

    public void uploadObject(String path, MultipartFile file) {
        String uploadPath = PathUtils.normalizePathAsMinioKey(path + file.getOriginalFilename());
        if (!PathUtils.isPathValid(uploadPath))
            throw new InvalidFilenameMinioException("Don't support symbols in filename: %s"
                    .formatted(file.getOriginalFilename()));

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uploadPath)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
        } catch (ErrorResponseException e) {
            throw new ResourceNotFoundMinioException(e);
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    public void deleteObject(String path) {
        path = PathUtils.normalizePathAsMinioKey(path);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build());
        } catch (ErrorResponseException e) {
            throw new ResourceNotFoundMinioException(e);
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }

    public void createEmptyFile(String path) {
        path = PathUtils.normalizePathAsMinioKey(path);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build());
        } catch (ErrorResponseException e) {
            throw new ResourceNotFoundMinioException(e);
        } catch (Exception e) {
            throw new MinioException(e);
        }
    }
}