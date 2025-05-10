package org.example.cloudstorage.minio.impl;

import io.minio.MinioClient;
import org.example.cloudstorage.config.MinioTestContainer;
import org.example.cloudstorage.exception.minio.ResourceNotFoundMinioException;
import org.example.cloudstorage.model.ObjectMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@Testcontainers
class MinioMetadataServiceImplTest {
    public static final String BUCKET_NAME = "user-files";
    private final MinioRepository minioRepository;
    private final MinioMetadataServiceImpl minioMetadataService;

    @Container
    static MinioTestContainer minioContainer = new MinioTestContainer();


    public MinioMetadataServiceImplTest() throws Exception {
        MinioClient minioClient = minioContainer.getMinioClient(BUCKET_NAME);
        this.minioRepository = new MinioRepository(minioClient, BUCKET_NAME);
        this.minioMetadataService = new MinioMetadataServiceImpl(minioRepository, "$");
    }

    private static Stream<Arguments> getResourceTestData() {
        return Stream.of(
                Arguments.of("user-2-files/", new ObjectMetadata(
                        "user-2-files/", true, 0L
                )),
                Arguments.of("user-2-files", new ObjectMetadata(
                        "user-2-files", false, 0L
                )),
                Arguments.of("user-2-files/folder1/file12.txt", new ObjectMetadata(
                        "user-2-files/folder1/file12.txt", false, 0L
                )),
                Arguments.of("user-2-files/folder1/", new ObjectMetadata(
                        "user-2-files/folder1/", true, 0L
                )));
    }

    @ParameterizedTest
    @DisplayName("Correctly get resources by given path")
    @MethodSource("getResourceTestData")
    void getResource(String input, ObjectMetadata expected) {
        List<String> objects = List.of(
                "$",
                "user-2-files",
                "user-2-files/$",
                "user-2-files/folder1/$",
                "user-2-files/folder1/file12.txt"
        );
        for (String object : objects) {
            minioRepository.createEmptyObject(object);
        }

        assertEquals(expected, minioMetadataService.getResource(input));
    }

    @Test
    @DisplayName("If resource doesnt exist at given path then throws ResourceNotFoundMinioException")
    void getResource_PathDoesntExist_Throws() {
        List<String> objects = List.of(
                "folder1/$",
                "folder2"
        );
        for (String object : objects) {
            minioRepository.createEmptyObject(object);
        }

        assertThrows(ResourceNotFoundMinioException.class,
                () -> minioMetadataService.getResource("folder1"));
        assertThrows(ResourceNotFoundMinioException.class,
                () -> minioMetadataService.getResource("folder2/"));
    }

    @Test
    void listFiles() {
        List<String> objects = List.of(
                "$",
                "user-2-files",
                "user-2-files/$",
                "user-2-files/folder1/$",
                "user-2-files/folder1/file12.txt"
        );
        for (String object : objects) {
            minioRepository.createEmptyObject(object);
        }

    }
}