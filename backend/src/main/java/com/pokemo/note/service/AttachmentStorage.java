package com.pokemo.note.service;

import java.nio.file.Path;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentStorage {

  String store(MultipartFile file, String originalName);

  void delete(String storedName);

  Path resolvePath(String storedName);
}
