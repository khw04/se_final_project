package com.pokemo.note.service;

import com.pokemo.common.ApiException;
import com.pokemo.note.api.AttachmentResponse;
import com.pokemo.note.domain.Attachment;
import com.pokemo.note.domain.Note;
import com.pokemo.note.repository.AttachmentRepository;
import com.pokemo.note.repository.NoteRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AttachmentService {

  private static final Path UPLOAD_DIR = Paths.get("uploads");

  private final AttachmentRepository attachmentRepository;
  private final NoteRepository noteRepository;

  public AttachmentService(AttachmentRepository attachmentRepository, NoteRepository noteRepository) {
    this.attachmentRepository = attachmentRepository;
    this.noteRepository = noteRepository;
    try {
      Files.createDirectories(UPLOAD_DIR);
    } catch (IOException e) {
      throw new RuntimeException("업로드 폴더 생성 실패", e);
    }
  }

  @Transactional
  public AttachmentResponse upload(Long userId, Long noteId, MultipartFile file) {
    Note note = noteRepository.findById(noteId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "노트를 찾을 수 없습니다."));
    if (!note.userId().equals(userId)) {
      throw new ApiException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
    }

    String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
    String ext = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')) : "";
    String storedName = UUID.randomUUID() + ext;

    try {
      Files.copy(file.getInputStream(), UPLOAD_DIR.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 실패");
    }

    Attachment attachment = new Attachment(noteId, originalName, storedName, file.getContentType(), file.getSize());
    return AttachmentResponse.from(attachmentRepository.save(attachment));
  }

  @Transactional(readOnly = true)
  public Attachment findForDownload(Long id) {
    return attachmentRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "첨부파일을 찾을 수 없습니다."));
  }

  public Path resolvePath(String storedName) {
    return UPLOAD_DIR.resolve(storedName);
  }
}
