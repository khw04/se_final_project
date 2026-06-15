package com.pokemo.note.service;

import com.pokemo.common.ApiException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@ConditionalOnProperty(name = "pokemo.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalAttachmentStorage implements AttachmentStorage {

  private final Path uploadDir;

  public LocalAttachmentStorage(@Value("${pokemo.storage.local.upload-dir:uploads}") String uploadDir) {
    this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    try {
      Files.createDirectories(this.uploadDir);
    } catch (IOException e) {
      throw new IllegalStateException("업로드 폴더 생성 실패", e);
    }
  }

  @Override
  public String store(MultipartFile file, String originalName) {
    String ext = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')) : "";
    String storedName = UUID.randomUUID() + ext;
    try {
      Files.copy(file.getInputStream(), resolvePath(storedName), StandardCopyOption.REPLACE_EXISTING);
      return storedName;
    } catch (IOException e) {
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 실패");
    }
  }

  @Override
  public void delete(String storedName) {
    try {
      Files.deleteIfExists(resolvePath(storedName));
    } catch (IOException exception) {
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "첨부파일 삭제 실패");
    }
  }

  @Override
  public Resource load(String storedName) {
    return new PathResource(resolvePath(storedName));
  }

  private Path resolvePath(String storedName) {
    return uploadDir.resolve(storedName).normalize();
  }
}
