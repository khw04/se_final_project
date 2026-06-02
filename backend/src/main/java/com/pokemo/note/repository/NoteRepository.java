package com.pokemo.note.repository;

import com.pokemo.note.domain.Note;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {

  List<Note> findByUserIdOrderByUpdatedAtDesc(Long userId);
}
