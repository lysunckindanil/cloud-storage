package org.example.cloudstorage.minio;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MinioRepository {
    private final MinioClient minioClient;
    private final String bucketName;

    public MinioRepository(MinioClient minioClient, String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
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


    public void upload(String path, Collection<MultipartFile> files) throws Exception {
        for (MultipartFile file : files) {
            uploadObject(path, file);
        }
    }


    public void createEmptyDirectory(String path) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                        .build());
    }


    public List<Item> getListObjects(String path, Boolean isRecursive) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        var result = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(path)
                        .recursive(isRecursive)
                        .build()
        );

        List<Item> items = new ArrayList<>();
        for (Result<Item> item : result) {
            items.add(item.get());
        }
        return items;
    }

    public void delete(String path) throws Exception {
        for (Item result : getListObjects(path, true)) {
            deleteObject(result.objectName());
        }
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