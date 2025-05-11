package org.example.cloudstorage.config;

import io.minio.MinioClient;
import org.example.cloudstorage.minio.MinioManagementFacade;
import org.example.cloudstorage.minio.impl.MinioRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;


@TestConfiguration
public class MinioMockConfig {
    @Bean
    public MinioClient minioClient() {
        return Mockito.mock(MinioClient.class);
    }

    @Bean
    public MinioManagementFacade minioRepository(MinioClient minioClient) {
        var repo = new MinioRepository(minioClient, "mock");
        return new MinioManagementFacade(repo);
    }
}
