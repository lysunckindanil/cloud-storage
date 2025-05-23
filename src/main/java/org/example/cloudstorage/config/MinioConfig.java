package org.example.cloudstorage.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import okhttp3.OkHttpClient;
import org.example.cloudstorage.config.properties.MinioProperties;
import org.example.cloudstorage.minio.MinioManagementFacade;
import org.example.cloudstorage.minio.impl.MinioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.TimeUnit;

@Profile("!test")
@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioProperties minioProperties) throws Exception {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.MINUTES)
                .build();

        var client = MinioClient.builder()
                .endpoint(minioProperties.getUrl())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .httpClient(httpClient)
                .build();
        createBucketIfNotExists(client, minioProperties.getBucketName());
        return client;
    }

    @Bean
    public MinioManagementFacade minioRepository(MinioClient minioClient, MinioProperties minioProperties) {
        return new MinioManagementFacade(new MinioRepository(minioClient, minioProperties.getBucketName()));
    }

    private static void createBucketIfNotExists(MinioClient minioClient, String bucketName) throws Exception {
        boolean found =
                minioClient.bucketExists(
                        BucketExistsArgs
                                .builder()
                                .bucket(bucketName)
                                .build()
                );

        if (found) return;

        minioClient.makeBucket(
                MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
    }
}
