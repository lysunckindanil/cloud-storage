package org.example.cloudstorage.config;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

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
}