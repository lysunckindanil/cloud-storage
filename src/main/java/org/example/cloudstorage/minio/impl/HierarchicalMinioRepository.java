package org.example.cloudstorage.minio.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.minio.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Slf4j
public class HierarchicalMinioRepository implements
        MinioResourceMetadataService,
        MinioResourceSearchService,
        MinioResourceDownloadService,
        MinioResourceManipulationService {

    private static final String DEFAULT_FOLDER_POSTFIX = "$";
    @Getter
    private final String folderPostfix;

    private final MinioResourceSearchService minioResourceSearchService;
    private final MinioResourceDownloadService minioResourceDownloadService;
    private final MinioResourceMetadataService minioResourceMetadataService;
    private final MinioResourceManipulationService minioResourceManipulationService;

    public HierarchicalMinioRepository(MinioRepository minioRepository) {
        this(minioRepository, DEFAULT_FOLDER_POSTFIX);
    }

    public HierarchicalMinioRepository(MinioRepository minioRepository, String folderPostfix) {
        this.folderPostfix = folderPostfix;
        this.minioResourceMetadataService = new MinioResourceMetadataServiceImpl(minioRepository, folderPostfix);
        this.minioResourceSearchService = new MinioResourceSearchServiceImpl(minioRepository, folderPostfix);
        this.minioResourceDownloadService = new MinioResourceDownloadServiceImpl(minioRepository, folderPostfix);
        this.minioResourceManipulationService = new MinioResourceManipulationServiceImpl(minioRepository, folderPostfix);
    }

    public ObjectMetadata get(String path) {
        return minioResourceMetadataService.get(path);
    }

    public List<ObjectMetadata> list(String path, boolean recursive) {
        return minioResourceMetadataService.list(path, recursive);
    }

    public InputStreamResource download(String path) {
        return minioResourceDownloadService.download(path);
    }

    public List<ObjectMetadata> search(String path, String query) {
        return minioResourceSearchService.search(path, query);
    }

    public void upload(String path, List<MultipartFile> files) {
        minioResourceManipulationService.upload(path, files);
    }

    public void delete(String path) {
        minioResourceManipulationService.delete(path);
    }

    public ObjectMetadata move(String from, String to) {
        return minioResourceManipulationService.move(from, to);
    }
}