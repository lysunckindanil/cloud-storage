package org.example.cloudstorage.repo;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MinioRepository {
    private final MinioClient minioClient;
}
