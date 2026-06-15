package com.pokemo.note.repository;

import com.pokemo.note.domain.NoteVersion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteVersionRepository extends JpaRepository<NoteVersion, Long> {

  List<NoteVersion> findByNoteIdAndUserIdOrderByVersionNumberDesc(Long noteId, Long userId);

  Optional<NoteVersion> findByIdAndNoteIdAndUserId(Long id, Long noteId, Long userId);

  int countByNoteId(Long noteId);

  void deleteByNoteId(Long noteId);
}
