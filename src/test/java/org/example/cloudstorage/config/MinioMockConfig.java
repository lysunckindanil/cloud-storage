package org.example.cloudstorage.config;

import io.minio.MinioClient;
import org.example.cloudstorage.minio.HierarchicalMinioRepository;
import org.example.cloudstorage.minio.MinioRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;


@TestConfiguration
public class MinioMockConfig {
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint("https://example.com")
                .credentials("username", "password")
                .build();
    }

    @Bean
    public HierarchicalMinioRepository minioRepository(MinioClient minioClient) {
        var repo = new MinioRepository(minioClient, "mock");
        return new HierarchicalMinioRepository(repo);
    }
}
