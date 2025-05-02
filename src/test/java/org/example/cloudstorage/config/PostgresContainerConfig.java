package org.example.cloudstorage.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class PostgresContainerConfig {
    @Bean
    @ServiceConnection
    public PostgreSQLContainer postgresContainer() {
        try (PostgreSQLContainer postgreSQLContainer =
                     new PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine"))) {
            return postgreSQLContainer;
        }
    }
}
