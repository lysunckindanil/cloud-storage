package org.example.cloudstorage.config;

import io.minio.MinioClient;
import org.example.cloudstorage.config.properties.MinioProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class MinioTestContainer extends GenericContainer<MinioTestContainer> {
    private static final int MINIO_PORT = 9000;
    private static final String DEFAULT_ACCESS_KEY = "minioadmin";
    private static final String DEFAULT_SECRET_KEY = "minioadmin";

    @SuppressWarnings("resource")
    public MinioTestContainer() {
        super(DockerImageName.parse("minio/minio:latest"));
        withExposedPorts(MINIO_PORT);
        withCommand("server /data");
        waitingFor(Wait.forHttp("/minio/health/ready").forPort(MINIO_PORT));
    }

    public String getUrl() {
        return String.format("http://%s:%s", getHost(), getMappedPort(MINIO_PORT));
    }

    public String getAccessKey() {
        return DEFAULT_ACCESS_KEY;
    }

    public String getSecretKey() {
        return DEFAULT_SECRET_KEY;
    }

    public MinioClient getMinioClient(String bucketName) throws Exception {
        MinioProperties minioProperties = new MinioProperties();
        minioProperties.setUrl(getUrl());
        minioProperties.setAccessKey(getAccessKey());
        minioProperties.setSecretKey(getSecretKey());
        minioProperties.setBucketName(bucketName);
        return new MinioConfig().minioClient(minioProperties);
    }
}