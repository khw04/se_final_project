package com.pokemo.note.api;

import com.pokemo.common.CurrentUserProvider;
import com.pokemo.note.domain.Attachment;
import com.pokemo.note.service.AttachmentService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class AttachmentController {

  private final AttachmentService attachmentService;
  private final CurrentUserProvider currentUserProvider;

  public AttachmentController(AttachmentService attachmentService, CurrentUserProvider currentUserProvider) {
    this.attachmentService = attachmentService;
    this.currentUserProvider = currentUserProvider;
  }

  @PostMapping("/notes/{noteId}/attachments")
  @ResponseStatus(HttpStatus.CREATED)
  AttachmentResponse upload(
      Principal principal,
      @PathVariable Long noteId,
      @RequestParam("file") MultipartFile file
  ) {
    long userId = currentUserProvider.userId(principal);
    return attachmentService.upload(userId, noteId, file);
  }

  @GetMapping("/attachments/{id}")
  ResponseEntity<Resource> download(@PathVariable Long id) throws IOException {
    Attachment attachment = attachmentService.findForDownload(id);
    Path path = attachmentService.resolvePath(attachment.storedName());
    Resource resource = new PathResource(path);
    String contentType = attachment.mimeType() != null ? attachment.mimeType() : "application/octet-stream";
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + attachment.fileName() + "\"")
        .body(resource);
  }
}
