package org.example.cloudstorage.minio;

import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.exception.minio.*;
import org.example.cloudstorage.util.PathUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Slf4j
@RequiredArgsConstructor
public class HierarchicalMinioRepository {

    private static final String DEFAULT_FOLDER_POSTFIX = "$";
    @Getter
    @Setter
    private String folderPostfix = DEFAULT_FOLDER_POSTFIX;
    private final MinioRepository minioRepository;

    public ObjectMetadata getResource(String path) {
        boolean isDir = path.endsWith("/");
        StatObjectResponse statObject = minioRepository.getObject(
                isDir ? path + folderPostfix : path
        );
        return new ObjectMetadata(
                statObject.object(),
                isDir,
                statObject.size()
        );
    }

    public List<ObjectMetadata> listResources(String path, boolean recursive) {
        if (!existsByPath(path)) throw new ResourceNotFoundMinioException("Folder is not found");
        return minioRepository.getListObjects(path, recursive)
                .stream()
                .filter(item -> !item.objectName().endsWith(folderPostfix))
                .map(item -> new ObjectMetadata(item.objectName(), item.isDir(), item.size()))
                .toList();
    }

    public void upload(String path, Collection<MultipartFile> files) {
        for (MultipartFile file : files) {
            if (file.getOriginalFilename() == null) throw new InvalidPathMinioException("Invalid filename");
            createMissingDirectories(path, file.getOriginalFilename());
            minioRepository.uploadObject(path, file);
        }
    }

    public InputStreamResource download(String path) {
        if (path.endsWith("/")) {
            return downloadAsZip(path);
        }
        return new InputStreamResource(minioRepository.downloadObject(path));
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

    public void createEmptyDirectory(String path) {
        minioRepository.createEmptyObject(path + folderPostfix);
    }

    public List<ObjectMetadata> search(String path, String query) {
        List<Item> objects = minioRepository.getListObjects(path, true);
        List<ObjectMetadata> result = new ArrayList<>();
        for (Item item : objects) {
            String objectName = item.objectName();
            if (objectName.equals(path + folderPostfix)) continue;
            boolean isDir = false;
            String objectSimpleName;

            if (objectName.endsWith(folderPostfix)) {
                objectSimpleName = PathUtils.getOneParentFromEndAtN(objectName, 1);
                isDir = true;
            } else {
                objectSimpleName = PathUtils.getOneParentFromEndAtN(objectName, 0);
            }

            if (objectSimpleName.toLowerCase().contains(query.toLowerCase())) {
                result.add(new ObjectMetadata(
                        isDir ? objectName.substring(0, objectName.length() - folderPostfix.length()) : objectName,
                        isDir,
                        item.size()));
            }
        }
        return result;
    }

    public ObjectMetadata move(String from, String to) {
        if (!from.endsWith("/")) {
            if (existsByPath(to))
                throw new ResourceAlreadyExistsMinioException("Destination already exists");
            createMissingDirectories("", to);
            StatObjectResponse statObjectResponse = minioRepository.getObject(from);
            minioRepository.copy(statObjectResponse.object(), to);
            minioRepository.deleteObject(statObjectResponse.object());
            return new ObjectMetadata(to, false, statObjectResponse.size());

        } else {
            List<Item> objects = minioRepository.getListObjects(from, true);
            for (Item item : objects) {
                if (existsByPath(to + item.objectName().substring(from.length()))) {
                    throw new ResourceAlreadyExistsMinioException("Destination already exists");
                }
                minioRepository.copy(item.objectName(), to + item.objectName().substring(from.length()));
                minioRepository.deleteObject(item.objectName());
            }
            return new ObjectMetadata(to, true, 0L);
        }
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
            path = path + folderPostfix;
        }
        try {
            minioRepository.getObject(path);
        } catch (ResourceNotFoundMinioException e) {
            return false;
        }
        return true;
    }

    private void createMissingDirectories(String path, String additionDir) {
        if (!additionDir.contains("/"))
            return;

        String[] dirs = additionDir.substring(0, additionDir.lastIndexOf("/")).split("/");
        StringJoiner joiner = new StringJoiner("/");
        for (String dir : dirs) {
            joiner.add(dir);
            createEmptyDirectory(path + joiner + "/");
        }
    }
}