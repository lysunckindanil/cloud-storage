package org.example.cloudstorage.minio;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.example.cloudstorage.constant.AppConstants.FOLDER_PREFIX;

public class MinioRepository {
    private final MinioClient minioClient;
    private final String bucketName;

    public MinioRepository(MinioClient minioClient, String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }


    public void upload(String path, Collection<MultipartFile> files) throws Exception {
        for (MultipartFile file : files) {
            uploadObject(path, file);
        }
    }

    public void delete(String path) throws Exception {

        for (Result<Item> result : getListObjects(path, true)) {
            deleteObject(result.get().objectName());
        }
    }

    public boolean existsByPath(String path) throws Exception {
        try {
            if (path.endsWith("/")) {
                path = path + FOLDER_PREFIX;
            }
            getObject(path);
        } catch (ErrorResponseException e) {
            return false;
        }
        return true;
    }

    public List<Item> getList(String path, boolean recursive) throws Exception {
        var result = getListObjects(path, recursive);
        List<Item> items = new ArrayList<>();
        for (Result<Item> item : result) {
            if (item.get().objectName().endsWith(FOLDER_PREFIX)) continue;
            items.add(item.get());
        }
        return items;
    }

    public void createEmptyDirectory(String path) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path + "/" + FOLDER_PREFIX)
                        .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                        .build());
    }

    public InputStreamResource download(String path) throws Exception {
        return new InputStreamResource(
                minioClient.getObject(GetObjectArgs
                        .builder()
                        .bucket(bucketName)
                        .object(path)
                        .build())
        );
    }

    public StatObjectResponse getObject(String path) throws Exception {
        return minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .build());
    }

    private Iterable<Result<Item>> getListObjects(String path, boolean recursive) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(path)
                        .recursive(recursive)
                        .build()
        );
    }

    private void uploadObject(String path, MultipartFile file) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path + file.getOriginalFilename())
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
    }

    private void deleteObject(String path) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .build());
    }
}