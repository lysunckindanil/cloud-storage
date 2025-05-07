package org.example.cloudstorage.minio;

import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.exception.minio.MinioException;
import org.example.cloudstorage.exception.minio.PartialDeletionMinioException;
import org.example.cloudstorage.exception.minio.ResourceNotFoundMinioException;
import org.example.cloudstorage.util.PathUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Slf4j
public class HierarchicalMinioRepository {
    private static final String MINIO_FOLDER_POSTFIX = "$";

    private final MinioRepository minioRepository;

    public HierarchicalMinioRepository(MinioRepository minioRepository) {
        this.minioRepository = minioRepository;
    }

    public ObjectMetadata getResource(String path) {
        boolean isDir = path.endsWith("/");
        StatObjectResponse statObject = minioRepository.getObject(
                isDir ? path + MINIO_FOLDER_POSTFIX : path
        );

        return new ObjectMetadata(
                statObject.object(),
                isDir,
                statObject.size()
        );
    }

    public List<ObjectMetadata> listResources(String path, boolean recursive) {
        if (!existsByPath(path)) throw new ResourceNotFoundMinioException("Folder is not found");
        return minioRepository.getListObjects(path, recursive).stream()
                .filter(item -> !item.objectName().endsWith(MINIO_FOLDER_POSTFIX))
                .map(item -> new ObjectMetadata(item.objectName(), item.isDir(), item.size()))
                .toList();
    }

    public void upload(String path, Collection<MultipartFile> files) {
        for (MultipartFile file : files) {
            minioRepository.uploadObject(path, file);
        }
    }

    public void delete(String path) {
        try {
            for (Item item : minioRepository.getListObjects(path, true)) {
                minioRepository.deleteObject(item.objectName());
            }
        } catch (MinioException e) {
            throw new PartialDeletionMinioException(e);
        }
    }

    public InputStreamResource download(String path) {
        if (!path.endsWith("/")) {
            return downloadAsZip(path);
        }
        return new InputStreamResource(minioRepository.downloadObject(path));
    }

    public void createEmptyDirectory(String path) {
        minioRepository.createEmptyObject(path + MINIO_FOLDER_POSTFIX);
    }

    private InputStreamResource downloadAsZip(String path) {
        String downloadPath = PathUtils.normalizePathAsMinioKey(path);

        PipedOutputStream out = new PipedOutputStream();
        new Thread(() -> {
            try (ZipOutputStream zipOut = new ZipOutputStream(out)) {
                for (Item item : minioRepository.getListObjects(downloadPath, true)) {
                    if (!item.isDir()) {
                        String objectName = item.objectName();
                        InputStream fileStream = minioRepository.downloadObject(objectName);

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

    private boolean existsByPath(String path) {
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

}