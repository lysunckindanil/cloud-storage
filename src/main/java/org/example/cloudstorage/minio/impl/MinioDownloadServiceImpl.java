package org.example.cloudstorage.minio.impl;

import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.exception.minio.MinioException;
import org.example.cloudstorage.minio.MinioDownloadService;
import org.example.cloudstorage.minio.MinioRepository;
import org.example.cloudstorage.util.PathUtils;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class MinioDownloadServiceImpl implements MinioDownloadService {

    private final MinioRepository minioRepository;
    private final String folderPostfix;

    public MinioDownloadServiceImpl(MinioRepository minioRepository, String folderPostfix) {
        this.minioRepository = minioRepository;
        this.folderPostfix = folderPostfix;
    }

    @Override
    public InputStreamResource downloadResource(String path) {
        if (path.endsWith("/")) {
            return downloadAsZip(path);
        }
        return new InputStreamResource(minioRepository.downloadObject(path));
    }


    private InputStreamResource downloadAsZip(String path) {
        String downloadPath = PathUtils.normalizePathMinioCompatible(path);

        PipedOutputStream out = new PipedOutputStream();
        new Thread(() -> {
            try (ZipOutputStream zipOut = new ZipOutputStream(out)) {
                for (Item item : minioRepository.getListObjects(downloadPath, true)) {
                    String objectName = item.objectName();
                    InputStream fileStream = minioRepository.downloadObject(objectName);

                    String entryName = item.objectName().endsWith(folderPostfix) ?
                            item.objectName().substring(downloadPath.length(), objectName.length() - folderPostfix.length()) :
                            item.objectName().substring(downloadPath.length());

                    zipOut.putNextEntry(new ZipEntry(entryName));

                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fileStream.read(buffer)) > 0) {
                        zipOut.write(buffer, 0, len);
                    }
                    zipOut.closeEntry();
                }
                zipOut.finish();
            } catch (Exception ex) {
                log.error("Error occurred while downloading object as zip", ex);
            }
        }).start();

        try {
            return new InputStreamResource(new PipedInputStream(out));
        } catch (IOException e) {
            throw new MinioException("Unable to download folder as zip", e);
        }
    }
}
