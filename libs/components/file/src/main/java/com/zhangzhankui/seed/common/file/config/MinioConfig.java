package com.zhangzhankui.seed.common.file.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** MinIO 配置 */
@Data
@AutoConfiguration
@EnableConfigurationProperties(MinioConfig.class)
@ConfigurationProperties(prefix = "minio")
@ConditionalOnProperty(
    prefix = "minio",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false)
public class MinioConfig {

  /** 是否启用 */
  private boolean enabled;

  /** 服务端点 */
  private String endpoint;

  /** Access Key */
  private String accessKey;

  /** Secret Key */
  private String secretKey;

  /** 默认桶名 */
  private String bucketName;

  @Bean
  public MinioClient minioClient() {
    return MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
  }
}
