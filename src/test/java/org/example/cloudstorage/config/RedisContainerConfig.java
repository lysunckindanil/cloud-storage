package org.example.cloudstorage.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@SuppressWarnings("resource")
@TestConfiguration
public class RedisContainerConfig {
    @Bean
    @ServiceConnection(name = "redis")
    public GenericContainer<?> redisContainer() {
        try (GenericContainer<?> redis = new GenericContainer<>(
                DockerImageName.parse("redis:7.0-alpine"))
                .withExposedPorts(6379)) {
            return redis;
        }
    }
}
