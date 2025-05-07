package org.example.cloudstorage.minio;

import io.minio.*;
import io.minio.messages.Item;
import org.example.cloudstorage.config.MinioConfig;
import org.example.cloudstorage.config.MinioTestContainer;
import org.example.cloudstorage.config.properties.MinioProperties;
import org.example.cloudstorage.exception.minio.ResourceAlreadyExistsMinioException;
import org.example.cloudstorage.exception.minio.ResourceNotFoundMinioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
class MinioRepositoryTest {

    public static final String BUCKET_NAME = "user-files";
    private final MinioRepository minioRepository;
    private final MinioClient minioClient;

    @Container
    static MinioTestContainer minioContainer = new MinioTestContainer();

    public MinioRepositoryTest() throws Exception {
        MinioProperties minioProperties = new MinioProperties();
        minioProperties.setUrl(minioContainer.getUrl());
        minioProperties.setAccessKey(minioContainer.getAccessKey());
        minioProperties.setSecretKey(minioContainer.getSecretKey());
        minioProperties.setBucketName(BUCKET_NAME);
        System.out.println(minioProperties.getAccessKey());
        System.out.println(minioProperties.getSecretKey());
        this.minioClient = new MinioConfig().minioClient(minioProperties);
        this.minioRepository = new MinioRepository(this.minioClient, minioProperties.getBucketName());
    }

    @BeforeEach
    void setUp() throws Exception {
        clearBucket();
    }

    @ParameterizedTest
    @CsvSource({
            "test.txt",
            "test/test1.txt",
            "test/"
    })
    @DisplayName("getObject return valid object if exists")
    void getObject(String objectName) throws Exception {
        createObject(objectName);
        assertEquals(objectName, minioRepository.getObject(objectName).object());
    }


    @Test
    @DisplayName("getObject throws ResourceNotFound if object doesn't exist")
    void getObject_DoesntExists_ThrowsException() throws Exception {
        String objectName = "test.txt";
        createObject(objectName);
        assertThrows(ResourceNotFoundMinioException.class, () -> minioRepository.getObject("123"));
    }


    @Test
    @DisplayName("getListObject if called not recursively returns exactly match ")
    void getListObjects_NotRecursively_ReturnsOnlyExactlyMatch() throws Exception {
        createObject("folder/obj1");
        createObject("folder/obj2");
        createObject("folder");
        var listObjects = minioRepository.getListObjects("folder", false);
        assertEquals(1, listObjects.size());
        assertTrue(listObjects.stream().anyMatch(x -> x.objectName().equals("folder")));
    }

    @Test
    @DisplayName("getListObject if called recursively returns all ")
    void getListObjects_Recursively_ReturnsAll() throws Exception {
        createObject("folder/obj1");
        createObject("folder/obj2");
        createObject("folder/obj3");
        var listObjects = minioRepository.getListObjects("folder", true);
        assertEquals(3, listObjects.size());
        assertTrue(listObjects.stream().anyMatch(x -> x.objectName().equals("folder/obj1")));
        assertTrue(listObjects.stream().anyMatch(x -> x.objectName().equals("folder/obj2")));
        assertTrue(listObjects.stream().anyMatch(x -> x.objectName().equals("folder/obj3")));
    }

    @Test
    @DisplayName("getListObject to be mentioned that existence of object with name of folder breaks all ")
    void getListObjects_Recursively_ReturnsNotAll() throws Exception {
        createObject("folder/obj1");
        createObject("folder/obj2");
        createObject("folder/obj3");
        createObject("folder");
        var listObjects = minioRepository.getListObjects("folder/", true);
        assertEquals(0, listObjects.size());

        var listObjects2 = minioRepository.getListObjects("folder", true);
        assertEquals(1, listObjects2.size());
    }

    @Test
    @DisplayName("downloadObject returns InputStream for existing object")
    void downloadObject_ObjectExists_ReturnsInputStream() throws Exception {
        String objectName = "test-download.txt";
        byte[] content = "Test file content".getBytes();
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(objectName)
                        .stream(new ByteArrayInputStream(content), content.length, -1)
                        .build()
        );

        InputStream downloadedStream = minioRepository.downloadObject(objectName);

        assertArrayEquals(content, downloadedStream.readAllBytes());
    }

    @Test
    @DisplayName("downloadObject throws ResourceNotFoundMinioException for non-existent object")
    void downloadObject_ThrowsException_WhenObjectNotExists() {
        assertThrows(ResourceNotFoundMinioException.class,
                () -> minioRepository.downloadObject("non-existent-file.txt"));
    }

    @Test
    @DisplayName("uploadObject successfully uploads file")
    void uploadObject_UploadsFile_WhenPathIsValid() throws Exception {
        String path = "test-upload/";
        String fileName = "test.txt";
        byte[] content = "Test content".getBytes();

        MultipartFile file = new MockMultipartFile(
                fileName,
                fileName,
                "text/plain",
                content
        );

        minioRepository.uploadObject(path, file);

        InputStream downloaded = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(path + fileName)
                        .build()
        );
        assertArrayEquals(content, downloaded.readAllBytes());
    }

    @Test
    @DisplayName("uploadObject throws ResourceAlreadyExists if file exists")
    void uploadObject_ThrowsException_WhenFileExists() throws Exception {
        String path = "test-upload/";
        String fileName = "duplicate.txt";
        byte[] content = "Test".getBytes();

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(path + fileName)
                        .stream(new ByteArrayInputStream(content), content.length, -1)
                        .build()
        );

        MultipartFile file = new MockMultipartFile(
                fileName,
                fileName,
                "text/plain",
                content
        );

        assertThrows(ResourceAlreadyExistsMinioException.class,
                () -> minioRepository.uploadObject(path, file));
    }

    @ParameterizedTest
    @CsvSource({
            "test.txt",
            "test/test1.txt",
            "test/"
    })
    @DisplayName("deleteObject after deletion the object is not found")
    void deleteObject(String objectName) throws Exception {
        createObject(objectName);
        assertEquals(objectName, minioRepository.getObject(objectName).object());
        minioRepository.deleteObject(objectName);
        assertThrows(ResourceNotFoundMinioException.class, () -> minioRepository.getObject(objectName));
    }

    @Test
    @DisplayName("deleteObject throws error if doesn't exist")
    void deleteObject_doesntExist_ThrowsError() {
        String objectName = "test.txt";
        assertThrows(ResourceNotFoundMinioException.class, () -> minioRepository.deleteObject(objectName));
    }

    @ParameterizedTest
    @CsvSource({
            "test.txt",
            "test/test1.txt",
            "test/"
    })
    void createEmptyObject(String objectName) throws Exception {
        createObject(objectName);
        assertEquals(objectName, minioRepository.getObject(objectName).object());
    }

    void createObject(String path) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(path)
                        .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                        .build());
    }

    void clearBucket() throws Exception {
        Iterable<Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(BUCKET_NAME)
                        .recursive(true)
                        .build()
        );

        for (Result<Item> result : objects) {
            Item item = result.get();
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(item.objectName())
                            .build()
            );
        }
    }
}