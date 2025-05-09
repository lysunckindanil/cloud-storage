package org.example.cloudstorage.minio.impl;

import io.minio.MinioClient;
import org.example.cloudstorage.config.MinioConfig;
import org.example.cloudstorage.config.MinioTestContainer;
import org.example.cloudstorage.config.properties.MinioProperties;
import org.example.cloudstorage.minio.MinioRepository;
import org.example.cloudstorage.minio.ObjectMetadata;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class MinioSearchServiceImplTest {

    public static final String BUCKET_NAME = "user-files";
    private final MinioRepository minioRepository;
    private final MinioSearchServiceImpl minioSearchService;

    @Container
    static MinioTestContainer minioContainer = new MinioTestContainer();

    MinioSearchServiceImplTest() throws Exception {
        MinioProperties minioProperties = new MinioProperties();
        minioProperties.setUrl(minioContainer.getUrl());
        minioProperties.setAccessKey(minioContainer.getAccessKey());
        minioProperties.setSecretKey(minioContainer.getSecretKey());
        minioProperties.setBucketName(BUCKET_NAME);
        MinioClient minioClient = new MinioConfig().minioClient(minioProperties);
        this.minioRepository = new MinioRepository(minioClient, minioProperties.getBucketName());
        this.minioSearchService = new MinioSearchServiceImpl(minioRepository, "$");

    }

    @Test
    void searchResources() {
        minioRepository.createEmptyObject("folder/$");
        minioRepository.createEmptyObject("folder/File.txt");
        minioRepository.createEmptyObject("folder/Foo.txt");
        minioRepository.createEmptyObject("folder/Minio.txt");
        minioRepository.createEmptyObject("folder/Minio/$");
        minioRepository.createEmptyObject("folder/Minio/File.txt");

        List<ObjectMetadata> result0 = minioSearchService.searchResources("", "txt");
        List<ObjectMetadata> result1 = minioSearchService.searchResources("", "folder");
        List<ObjectMetadata> result2 = minioSearchService.searchResources("folder/", "folder");
        List<ObjectMetadata> result3 = minioSearchService.searchResources("folder/", "txt");
        List<ObjectMetadata> result4 = minioSearchService.searchResources("folder/", "minio");
        List<ObjectMetadata> result5 = minioSearchService.searchResources("folder/Minio", "file");

        assertTrue(areListsEqualIgnoringOrder(
                List.of(
                        new ObjectMetadata("folder/File.txt", false, 0L),
                        new ObjectMetadata("folder/Foo.txt", false, 0L),
                        new ObjectMetadata("folder/Minio.txt", false, 0L),
                        new ObjectMetadata("folder/Minio/File.txt", false, 0L)
                ),
                result0));

        assertTrue(areListsEqualIgnoringOrder(
                List.of(
                        new ObjectMetadata("folder/", true, 0L)
                ),
                result1));

        assertTrue(areListsEqualIgnoringOrder(
                List.of(),
                result2));

        assertTrue(areListsEqualIgnoringOrder(
                List.of(
                        new ObjectMetadata("folder/File.txt", false, 0L),
                        new ObjectMetadata("folder/Foo.txt", false, 0L),
                        new ObjectMetadata("folder/Minio.txt", false, 0L),
                        new ObjectMetadata("folder/Minio/File.txt", false, 0L)
                ),
                result3));

        assertTrue(areListsEqualIgnoringOrder(
                List.of(
                        new ObjectMetadata("folder/Minio.txt", false, 0L),
                        new ObjectMetadata("folder/Minio/", true, 0L)
                ),
                result4));

        assertTrue(areListsEqualIgnoringOrder(
                List.of(
                        new ObjectMetadata("folder/Minio/File.txt", false, 0L)
                ),
                result5));
    }

    public static <T> boolean areListsEqualIgnoringOrder(List<T> list1, List<T> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        Map<T, Long> frequency1 = list1.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        Map<T, Long> frequency2 = list2.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        return frequency1.equals(frequency2);
    }
}