package com.pokemo.note.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentStorage {

  String store(MultipartFile file, String originalName);

  void delete(String storedName);

  Resource load(String storedName);
}
