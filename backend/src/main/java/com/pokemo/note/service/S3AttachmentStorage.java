package com.pokemo.note.service;

import com.pokemo.common.ApiException;
import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@ConditionalOnProperty(name = "pokemo.storage.type", havingValue = "s3")
public class S3AttachmentStorage implements AttachmentStorage {

  private final S3Client s3Client;
  private final String bucket;
  private final String prefix;

  public S3AttachmentStorage(
      @Value("${pokemo.storage.s3.bucket}") String bucket,
      @Value("${pokemo.storage.s3.region}") String region,
      @Value("${pokemo.storage.s3.prefix:attachments}") String prefix
  ) {
    if (bucket == null || bucket.isBlank()) {
      throw new IllegalStateException("S3_BUCKET must be configured when STORAGE_TYPE=s3");
    }
    this.bucket = bucket;
    this.prefix = prefix == null || prefix.isBlank() ? "attachments" : prefix.replaceAll("/+$", "");
    this.s3Client = S3Client.builder()
        .region(Region.of(region))
        .build();
  }

  @Override
  public String store(MultipartFile file, String originalName) {
    String ext = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')) : "";
    String storedName = UUID.randomUUID() + ext;
    String key = key(storedName);
    try {
      s3Client.putObject(PutObjectRequest.builder()
              .bucket(bucket)
              .key(key)
              .contentType(file.getContentType())
              .contentLength(file.getSize())
              .build(),
          RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
      return storedName;
    } catch (IOException exception) {
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 실패");
    }
  }

  @Override
  public void delete(String storedName) {
    s3Client.deleteObject(DeleteObjectRequest.builder()
        .bucket(bucket)
        .key(key(storedName))
        .build());
  }

  @Override
  public Resource load(String storedName) {
    return new InputStreamResource(s3Client.getObject(GetObjectRequest.builder()
        .bucket(bucket)
        .key(key(storedName))
        .build()));
  }

  private String key(String storedName) {
    return prefix + "/" + storedName;
  }
}
