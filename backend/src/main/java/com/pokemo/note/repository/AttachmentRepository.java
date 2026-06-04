package com.pokemo.note.repository;

import com.pokemo.note.domain.Attachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

  List<Attachment> findByNoteId(Long noteId);
}
