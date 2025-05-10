package org.example.cloudstorage.minio.impl;

import io.minio.MinioClient;
import io.minio.messages.Item;
import org.example.cloudstorage.config.MinioConfig;
import org.example.cloudstorage.config.MinioTestContainer;
import org.example.cloudstorage.config.properties.MinioProperties;
import org.example.cloudstorage.exception.minio.InvalidFileMinioException;
import org.example.cloudstorage.exception.minio.InvalidPathMinioException;
import org.example.cloudstorage.exception.minio.ResourceAlreadyExistsMinioException;
import org.example.cloudstorage.exception.minio.ResourceNotFoundMinioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@Testcontainers
class MinioManipulationServiceImplTest {
    public static final String BUCKET_NAME = "user-files";
    private final MinioRepository minioRepository;
    private final MinioManipulationServiceImpl minioManipulationService;

    @Container
    static MinioTestContainer minioContainer = new MinioTestContainer();

    public MinioManipulationServiceImplTest() throws Exception {
        MinioProperties minioProperties = new MinioProperties();
        minioProperties.setUrl(minioContainer.getUrl());
        minioProperties.setAccessKey(minioContainer.getAccessKey());
        minioProperties.setSecretKey(minioContainer.getSecretKey());
        minioProperties.setBucketName(BUCKET_NAME);
        MinioClient minioClient = new MinioConfig().minioClient(minioProperties);
        this.minioRepository = new MinioRepository(minioClient, minioProperties.getBucketName());
        this.minioManipulationService = new MinioManipulationServiceImpl(minioRepository, "$");
    }

    // uploadResource

    @Test
    @DisplayName("Uploading files with folders should create folders in repo as well")
    void uploadResources_createsFoldersCorrectly() {
        List<String> fileNames = List.of(
                "folder1/file11.txt",
                "folder1/file12.txt",
                "folder2/folder3/file21.txt",
                "folder2/folder3/file22.txt"
        );

        List<MultipartFile> multipartFiles = fileNames.stream()
                .map(name -> (MultipartFile) new MockMultipartFile(
                        name,
                        name,
                        "text/plain",
                        name.getBytes(StandardCharsets.UTF_8)
                )).toList();

        minioManipulationService.uploadResources("", multipartFiles);

        List<String> expectedObjects = List.of(
                "folder1/$",
                "folder1/file11.txt",
                "folder1/file12.txt",
                "folder2/$",
                "folder2/folder3/$",
                "folder2/folder3/file21.txt",
                "folder2/folder3/file22.txt"
        );

        List<String> actualObjects = minioRepository.getListObjects("", true)
                .stream()
                .map(Item::objectName)
                .toList();

        assertTrue(areListsEqualIgnoringOrder(expectedObjects, actualObjects));
    }

    @Test
    @DisplayName("If file doesnt have an OriginalFileName then InvalidFileMinioException thrown")
    void uploadResources_NoOriginalFileName_Throws() {
        MultipartFile file =
                new MockMultipartFile(
                        "filename",
                        null,
                        "text/plain",
                        "content".getBytes(StandardCharsets.UTF_8)
                );

        assertThrows(InvalidFileMinioException.class, () ->
                minioManipulationService.uploadResources("", List.of(file)));
    }


    @Test
    @DisplayName("If any exception happens while uploading then none will be uploaded")
    void uploadResources_exceptionThrown_NoneUploaded() {
        List<String> fileNames = List.of(
                "folder1/file11.txt",
                "folder1/file12.txt",
                "folder2/folder3/file21.txt",
                "folder2/folder3/file22.txt"
        );

        List<MultipartFile> multipartFiles = new java.util.ArrayList<>(fileNames.stream()
                .map(name -> (MultipartFile) new MockMultipartFile(
                        name,
                        name,
                        "text/plain",
                        name.getBytes(StandardCharsets.UTF_8)
                )).toList());

        MultipartFile file =
                new MockMultipartFile(
                        "filename",
                        null,
                        "text/plain",
                        "content".getBytes(StandardCharsets.UTF_8)
                );

        multipartFiles.add(file);
        assertThrows(InvalidFileMinioException.class, () ->
                minioManipulationService.uploadResources("", multipartFiles));

        assertEquals(0, minioRepository.getListObjects("", true).size());
    }

    // deleteResource

    @Test
    @DisplayName("deleteResource deletes given file from repository")
    void deleteResource_singleFile() {
        minioRepository.createEmptyObject("file.txt");
        minioManipulationService.deleteResource("file.txt");
        assertEquals(0, minioRepository.getListObjects("", true).size());

    }

    @Test
    @DisplayName("deleteResource deletes given directory and files inside from repository")
    void deleteResource_directory() {
        minioRepository.createEmptyObject("folder/$");
        minioRepository.createEmptyObject("folder/folder2/$");
        minioRepository.createEmptyObject("folder/folder2/file.txt");
        minioRepository.createEmptyObject("folder/file1.txt");
        minioManipulationService.deleteResource("folder/");
        assertEquals(0, minioRepository.getListObjects("", true).size());
    }

    @Test
    @DisplayName("deleteResource throws ResourceNotFoundMinioException if object does not exist")
    void deleteResource_resourceDoesNotExist_throws() {
        assertThrows(ResourceNotFoundMinioException.class, () ->
                minioManipulationService.deleteResource("file.txt"));
    }

    // moveResource

    private static Stream<Arguments> moveResourceTestData() {
        return Stream.of(
                Arguments.of("folder1/file11.txt", "folder1/file15.txt",
                        List.of(
                                "$",
                                "folder1/$",
                                "folder1/file12.txt",
                                "folder1/file15.txt",
                                "folder1/folder2/$",
                                "folder1/folder2/file21.txt",
                                "folder1/folder2/file22.txt"
                        )),
                Arguments.of("folder1/", "folder2/",
                        List.of(
                                "$",
                                "folder2/$",
                                "folder2/file11.txt",
                                "folder2/file12.txt",
                                "folder2/folder2/$",
                                "folder2/folder2/file21.txt",
                                "folder2/folder2/file22.txt"
                        )),
                Arguments.of("folder1/file11.txt", "folder1/folder2/file11.txt",
                        List.of(
                                "$",
                                "folder1/$",
                                "folder1/file12.txt",
                                "folder1/folder2/$",
                                "folder1/folder2/file11.txt",
                                "folder1/folder2/file21.txt",
                                "folder1/folder2/file22.txt"
                        )),
                Arguments.of("folder1/folder2/file22.txt", "folder1/file22.txt",
                        List.of(
                                "$",
                                "folder1/$",
                                "folder1/file11.txt",
                                "folder1/file12.txt",
                                "folder1/file22.txt",
                                "folder1/folder2/$",
                                "folder1/folder2/file21.txt"
                        )),
                Arguments.of("", "folder3/",
                        List.of(
                                "folder3/$",
                                "folder3/folder1/$",
                                "folder3/folder1/file11.txt",
                                "folder3/folder1/file12.txt",
                                "folder3/folder1/folder2/$",
                                "folder3/folder1/folder2/file21.txt",
                                "folder3/folder1/folder2/file22.txt"
                        ))
        );
    }

    @DisplayName("Checks whether files and dirs moved correctly")
    @ParameterizedTest
    @MethodSource("moveResourceTestData")
    void moveResource(String from, String to, List<String> expected) {
        List<String> objects = List.of(
                "$",
                "folder1/$",
                "folder1/file11.txt",
                "folder1/file12.txt",
                "folder1/folder2/$",
                "folder1/folder2/file21.txt",
                "folder1/folder2/file22.txt"
        );
        for (String object : objects) {
            minioRepository.createEmptyObject(object);
        }

        minioManipulationService.moveResource(from, to);

        List<String> actual =
                minioRepository.getListObjects("/", true)
                        .stream()
                        .map(Item::objectName)
                        .toList();
        System.out.println(actual);
        assertTrue(areListsEqualIgnoringOrder(expected, actual));
    }

    @Test
    @DisplayName("moveResource throws error if destination file or directory already exists")
    void moveResource_DestinationAlreadyExists_Throws() {
        List<String> objects = List.of(
                "folder1/$",
                "folder2/$",
                "folder1/file.txt",
                "folder2/file.txt"
        );
        for (String object : objects) {
            minioRepository.createEmptyObject(object);
        }

        assertThrows(ResourceAlreadyExistsMinioException.class,
                () -> minioManipulationService.moveResource("folder1/", "folder2/"));
        assertThrows(ResourceAlreadyExistsMinioException.class,
                () -> minioManipulationService.moveResource("folder1/file.txt", "folder2/file.txt"));
    }


    @Test
    @DisplayName("moveResource throws error if source file or directory already exists")
    void moveResource_SourceDoesntExist_Throws() {
        assertThrows(ResourceNotFoundMinioException.class,
                () -> minioManipulationService.moveResource("file.txt", "file2.txt"));
        assertThrows(ResourceNotFoundMinioException.class,
                () -> minioManipulationService.moveResource("folder1/", "folder2/"));
    }

    private static Stream<Arguments> moveResourceInvalidPathTestData() {
        return Stream.of(
                Arguments.of("file.txt", "file.txt"),
                Arguments.of("folder/", "folder/"),
                Arguments.of("", "file.txt"),
                Arguments.of("folder/", "file.txt"),
                Arguments.of("file.txt", "folder/"),
                Arguments.of("file.txt", "")
        );
    }

    @MethodSource("moveResourceInvalidPathTestData")
    @DisplayName("moveResource throws exception if not provided movement dir to dir and file to file")
    @ParameterizedTest
    void moveResource_InvalidPaths_Throws(String from, String to) {
        assertThrows(InvalidPathMinioException.class,
                () -> minioManipulationService.moveResource(from, to));
    }


    // createEmptyDirectory

    private static Stream<Arguments> createEmptyDirectoryTestData() {
        return Stream.of(
                Arguments.of("folder/", List.of("folder/$")),
                Arguments.of("folder/", List.of("folder/$")),
                Arguments.of("folder1/folder2/", List.of("folder1/$", "folder1/folder2/$")),
                Arguments.of("", List.of("$"))
        );
    }

    @MethodSource("createEmptyDirectoryTestData")
    @DisplayName("creates empty files with special folder symbol, creates missing dirs as well")
    @ParameterizedTest
    void createEmptyDirectory(String input, List<String> expected) {
        minioManipulationService.createEmptyDirectory(input, false);
        List<String> actual =
                minioRepository.getListObjects("", true)
                        .stream()
                        .map(Item::objectName)
                        .toList();

        assertTrue(areListsEqualIgnoringOrder(expected, actual));
    }

    @Test
    void createEmptyDirectory_alreadyExists_throwsError() {
        minioManipulationService.createEmptyDirectory("folder/", false);
        assertThrows(ResourceAlreadyExistsMinioException.class, () ->
                minioManipulationService.createEmptyDirectory("folder/", false));
    }

    @Test
    void createEmptyDirectory_doesntEndWithSlash_throwsError() {
        assertThrows(InvalidPathMinioException.class, () ->
                minioManipulationService.createEmptyDirectory("folder", false));
    }

    // utils

    @BeforeEach
    void setUp() {
        clearBucket();
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

    void clearBucket() {
        var objects = minioRepository.getListObjects("/", true);
        minioRepository.deleteObjects(objects.stream().map(Item::objectName).toList());
    }
}