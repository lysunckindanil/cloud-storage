package org.example.cloudstorage.minio;

import io.minio.*;
import io.minio.messages.Item;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.Collection;

public class MinioRepository {
    private final MinioClient minioClient;
    private final String bucketName;

    public MinioRepository(MinioClient minioClient, String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    public InputStreamResource download(String path) throws Exception {
        return new InputStreamResource(
                minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucketName)
                                .object(path)
                                .build())
        );
    }


    public void upload(String path, Collection<MultipartFile> files) throws Exception {
        for (MultipartFile file : files) {
            upload(path, file);
        }
    }

    private void upload(String path, MultipartFile file) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path + file.getOriginalFilename())
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
    }

    public void createEmptyDirectory(String path) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                        .build());
    }


    public void delete(String path) throws Exception {
        for (Result<Item> result : getListObjects(path, true)) {
            deleteObject(result.get().objectName());
        }
    }

    public Iterable<Result<Item>> getListObjects(String path, Boolean isRecursive) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(path)
                        .recursive(isRecursive)
                        .build()
        );
    }

    private void deleteObject(String path) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .build());
    }
}