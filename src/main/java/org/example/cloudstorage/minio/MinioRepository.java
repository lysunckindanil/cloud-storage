package org.example.cloudstorage.minio;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import org.example.cloudstorage.exception.MinioException;
import org.example.cloudstorage.exception.ResourceNotFoundMinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.example.cloudstorage.constant.AppConstants.MINIO_FOLDER_POSTFIX;

public class MinioRepository {

    Logger logger = LoggerFactory.getLogger(MinioRepository.class);

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioRepository(MinioClient minioClient, String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }


    public void upload(String path, Collection<MultipartFile> files) {
        try {
            for (MultipartFile file : files) {
                uploadObject(path, file);
            }
        } catch (Exception e) {
            throw new MinioException("Error occurred while uploading objects", e);
        }
    }

    public void delete(String path) {
        for (Item item : getListObjects(path, true)) {
            deleteObject(item.objectName());
        }
    }

    public boolean existsByPath(String path) {
        if (path.endsWith("/")) {
            path = path + MINIO_FOLDER_POSTFIX;
        }
        try {
            getObject(path);
        } catch (ResourceNotFoundMinioException e) {
            return false;
        }
        return true;
    }

    public List<Item> getList(String path, boolean recursive) {
        if (!existsByPath(path)) throw new ResourceNotFoundMinioException("Folder is not found");
        return getListObjects(path, recursive).stream()
                .filter(item -> !item.objectName().endsWith(MINIO_FOLDER_POSTFIX))
                .toList();
    }

    public InputStreamResource download(String path) {
        if (!path.endsWith("/")) {
            return new InputStreamResource(downloadObject(path));
        } else {
            return downloadAsZip(path);
        }
    }

    public Item getByPath(String path) {
        boolean isDir = path.endsWith("/");
        StatObjectResponse statObject = getObject(
                isDir ? path + MINIO_FOLDER_POSTFIX : path
        );

        return new Item() {
            @Override
            public boolean isDir() {
                return isDir;
            }

            @Override
            public long size() {
                return statObject.size();
            }

            @Override
            public String objectName() {
                return path;
            }
        };
    }

    public void createEmptyDirectory(String path) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path + MINIO_FOLDER_POSTFIX)
                        .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                        .build());
    }

    private InputStreamResource downloadAsZip(String path) {
        PipedOutputStream out = new PipedOutputStream();
        new Thread(() -> {
            try (ZipOutputStream zipOut = new ZipOutputStream(out)) {
                for (Item item : this.getListObjects(path, true)) {
                    if (!item.isDir()) {
                        String objectName = item.objectName();
                        var fileStream = downloadObject(objectName);

                        String entryName = item.objectName().endsWith(MINIO_FOLDER_POSTFIX) ?
                                item.objectName().substring(path.length(), objectName.length() - 1) :
                                item.objectName().substring(path.length());

                        zipOut.putNextEntry(new ZipEntry(entryName));

                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = fileStream.read(buffer)) > 0) {
                            zipOut.write(buffer, 0, len);
                        }
                        zipOut.closeEntry();
                    }
                }
                zipOut.finish();
            } catch (Exception ex) {
                logger.error("Error occurred while downloading object as zip", ex);
            }
        }).start();

        try {
            return new InputStreamResource(new PipedInputStream(out));
        } catch (IOException ex) {
            throw new MinioException("Unable to download folder as zip", ex);
        }
    }

    private StatObjectResponse getObject(String path) {
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

    private InputStream downloadObject(String path) {
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

    private List<Item> getListObjects(String path, boolean recursive) {
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

    private void uploadObject(String path, MultipartFile file) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path + file.getOriginalFilename())
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
    }

    private void deleteObject(String path) {
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
}