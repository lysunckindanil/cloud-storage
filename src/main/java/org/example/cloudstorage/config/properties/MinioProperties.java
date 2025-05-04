package org.example.cloudstorage.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.minio")
public class MinioProperties {
    private String url;
    private String accessKey;
    private String secretKey;
    private String bucketName;
}
