package com.pokemo.note.service;

import com.pokemo.common.ApiException;
import com.pokemo.note.api.AttachmentResponse;
import com.pokemo.note.domain.Attachment;
import com.pokemo.note.domain.Note;
import com.pokemo.note.repository.AttachmentRepository;
import com.pokemo.note.repository.NoteRepository;
import java.nio.file.Path;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AttachmentService {

  private final AttachmentRepository attachmentRepository;
  private final NoteRepository noteRepository;
  private final AttachmentStorage attachmentStorage;

  public AttachmentService(
      AttachmentRepository attachmentRepository,
      NoteRepository noteRepository,
      AttachmentStorage attachmentStorage
  ) {
    this.attachmentRepository = attachmentRepository;
    this.noteRepository = noteRepository;
    this.attachmentStorage = attachmentStorage;
  }

  @Transactional
  public AttachmentResponse upload(Long userId, Long noteId, MultipartFile file) {
    Note note = noteRepository.findById(noteId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "노트를 찾을 수 없습니다."));
    if (!note.userId().equals(userId)) {
      throw new ApiException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
    }

    String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
    String storedName = attachmentStorage.store(file, originalName);

    Attachment attachment = new Attachment(noteId, originalName, storedName, file.getContentType(), file.getSize());
    return AttachmentResponse.from(attachmentRepository.save(attachment));
  }

  @Transactional(readOnly = true)
  public Attachment findForDownload(Long id) {
    return attachmentRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "첨부파일을 찾을 수 없습니다."));
  }

  @Transactional
  public void delete(Long userId, Long id) {
    Attachment attachment = attachmentRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "첨부파일을 찾을 수 없습니다."));
    Note note = noteRepository.findById(attachment.noteId())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "노트를 찾을 수 없습니다."));
    if (!note.userId().equals(userId)) {
      throw new ApiException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
    }
    attachmentRepository.delete(attachment);
    attachmentStorage.delete(attachment.storedName());
  }

  public Path resolvePath(String storedName) {
    return attachmentStorage.resolvePath(storedName);
  }
}
