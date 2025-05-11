package org.example.cloudstorage.minio;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.minio.impl.*;
import org.example.cloudstorage.model.ObjectMetadata;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Slf4j
public class MinioManagementFacade {

    private static final String DEFAULT_FOLDER_POSTFIX = "$";
    @Getter
    private final String folderPostfix;

    private final MinioSearchService minioSearchService;
    private final MinioDownloadService minioDownloadService;
    private final MinioMetadataService minioMetadataService;
    private final MinioManipulationService minioManipulationService;

    public MinioManagementFacade(MinioRepository minioRepository) {
        this(minioRepository, DEFAULT_FOLDER_POSTFIX);
    }

    public MinioManagementFacade(MinioRepository minioRepository, String folderPostfix) {
        if (folderPostfix == null) throw new IllegalArgumentException("folderPostfix cannot be null");
        this.folderPostfix = folderPostfix;
        this.minioMetadataService = new MinioMetadataServiceImpl(minioRepository, folderPostfix);
        this.minioSearchService = new MinioSearchServiceImpl(minioRepository, folderPostfix);
        this.minioDownloadService = new MinioDownloadServiceImpl(minioRepository, folderPostfix);
        this.minioManipulationService = new MinioManipulationServiceImpl(minioRepository, folderPostfix);
    }


    public ObjectMetadata getResource(String path) {
        return minioMetadataService.getResource(path);
    }


    public List<ObjectMetadata> listFiles(String path, boolean recursive) {
        return minioMetadataService.listFiles(path, recursive);
    }


    public InputStreamResource downloadResource(String path) {
        return minioDownloadService.downloadResource(path);
    }


    public List<ObjectMetadata> searchResources(String path, String query) {
        return minioSearchService.searchResources(path, query);
    }


    public void uploadResource(String path, List<MultipartFile> files) {
        minioManipulationService.uploadResources(path, files);
    }


    public void deleteResource(String path) {
        minioManipulationService.deleteResource(path);
    }


    public ObjectMetadata moveResource(String from, String to) {
        return minioManipulationService.moveResource(from, to);
    }


    public void createEmptyDirectory(String path) {
        minioManipulationService.createEmptyDirectory(path, false);
    }
}