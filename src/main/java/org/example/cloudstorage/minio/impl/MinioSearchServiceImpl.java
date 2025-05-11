package org.example.cloudstorage.minio.impl;

import io.minio.messages.Item;
import org.example.cloudstorage.minio.MinioSearchService;
import org.example.cloudstorage.model.ResourceMetadata;
import org.example.cloudstorage.util.PathUtils;

import java.util.ArrayList;
import java.util.List;


public class MinioSearchServiceImpl implements MinioSearchService {

    private final MinioRepository minioRepository;
    private final String folderPostfix;

    public MinioSearchServiceImpl(MinioRepository minioRepository, String folderPostfix) {
        this.minioRepository = minioRepository;
        this.folderPostfix = folderPostfix;
    }


    @Override
    public List<ResourceMetadata> searchResources(String path, String query) {
        List<Item> objects = minioRepository.getListObjects(path, true);
        List<ResourceMetadata> result = new ArrayList<>();
        for (Item item : objects) {
            String objectName = item.objectName();
            if (objectName.equals(path + folderPostfix)) continue;
            String objectSimpleName;
            boolean isDir = false;

            if (objectName.endsWith(folderPostfix)) {
                objectSimpleName = PathUtils.getParentFromEndAtN(objectName, 1);
                isDir = true;
            } else {
                objectSimpleName = PathUtils.getParentFromEndAtN(objectName, 0);
            }

            if (objectSimpleName.toLowerCase().contains(query.toLowerCase())) {
                result.add(new ResourceMetadata(
                        isDir ? objectName.substring(0, objectName.length() - folderPostfix.length()) : objectName,
                        isDir,
                        item.size()));
            }
        }
        return result;
    }
}
