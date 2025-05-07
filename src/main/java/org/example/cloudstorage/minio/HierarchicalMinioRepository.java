package org.example.cloudstorage.minio;

import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.exception.MinioException;
import org.example.cloudstorage.exception.ResourceNotFoundMinioException;
import org.example.cloudstorage.util.PathUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Slf4j
public class HierarchicalMinioRepository {
    public static final String MINIO_FOLDER_POSTFIX = "$";

    private final MinioRepository minioRepository;

    public HierarchicalMinioRepository(MinioRepository minioRepository) {
        this.minioRepository = minioRepository;
    }

    public boolean existsByPath(String path) {
        path = PathUtils.normalizePathAsMinioKey(path);
        if (path.endsWith("/")) {
            path = path + MINIO_FOLDER_POSTFIX;
        }
        try {
            minioRepository.getObject(path);
        } catch (ResourceNotFoundMinioException e) {
            return false;
        }
        return true;
    }


    public List<Item> getList(String path, boolean recursive) {
        path = PathUtils.normalizePathAsMinioKey(path);
        if (!existsByPath(path)) throw new ResourceNotFoundMinioException("Folder is not found");
        return minioRepository.getListObjects(path, recursive).stream()
                .filter(item -> !item.objectName().endsWith(MINIO_FOLDER_POSTFIX))
                .toList();
    }

    public void upload(String path, Collection<MultipartFile> files) {
        for (MultipartFile file : files) {
            minioRepository.uploadObject(path, file);
        }
    }

    public void delete(String path) {
        for (Item item : minioRepository.getListObjects(path, true)) {
            minioRepository.deleteObject(item.objectName());
        }
    }


    public InputStreamResource download(String path) {
        if (!path.endsWith("/")) {
            return new InputStreamResource(minioRepository.downloadObject(path));
        } else {
            return downloadAsZip(path);
        }
    }

    public Item getByPath(String path) {
        boolean isDir = path.endsWith("/");
        StatObjectResponse statObject = minioRepository.getObject(
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
                return isDir
                        ?
                        statObject.object().substring(0, statObject.object().length() - MINIO_FOLDER_POSTFIX.length())
                        :
                        statObject.object();
            }
        };
    }

    public void createEmptyDirectory(String path) {
        path = PathUtils.normalizePathAsMinioKey(path);
        minioRepository.createEmptyFile(path + MINIO_FOLDER_POSTFIX);
    }

    private InputStreamResource downloadAsZip(String path) {
        String downloadPath = PathUtils.normalizePathAsMinioKey(path);

        PipedOutputStream out = new PipedOutputStream();
        new Thread(() -> {
            try (ZipOutputStream zipOut = new ZipOutputStream(out)) {
                for (Item item : minioRepository.getListObjects(downloadPath, true)) {
                    if (!item.isDir()) {
                        String objectName = item.objectName();
                        var fileStream = minioRepository.downloadObject(objectName);

                        String entryName = item.objectName().endsWith(MINIO_FOLDER_POSTFIX) ?
                                item.objectName().substring(downloadPath.length(), objectName.length() - MINIO_FOLDER_POSTFIX.length()) :
                                item.objectName().substring(downloadPath.length());

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
                log.error("Error occurred while downloading object as zip", ex);
            }
        }).start();

        try {
            return new InputStreamResource(new PipedInputStream(out));
        } catch (IOException ex) {
            throw new MinioException("Unable to download folder as zip", ex);
        }
    }
}
