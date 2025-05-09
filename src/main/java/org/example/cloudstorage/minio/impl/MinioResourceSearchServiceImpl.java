package org.example.cloudstorage.minio.impl;

import io.minio.messages.Item;
import org.example.cloudstorage.minio.MinioRepository;
import org.example.cloudstorage.minio.MinioResourceSearchService;
import org.example.cloudstorage.minio.ObjectMetadata;
import org.example.cloudstorage.util.PathUtils;

import java.util.ArrayList;
import java.util.List;


public class MinioResourceSearchServiceImpl implements MinioResourceSearchService {

    private final MinioRepository minioRepository;
    private final String folderPostfix;

    MinioResourceSearchServiceImpl(MinioRepository minioRepository, String folderPostfix) {
        this.minioRepository = minioRepository;
        this.folderPostfix = folderPostfix;
    }


    @Override
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
}
