package com.pokemo.note.repository;

import com.pokemo.note.domain.Note;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoteRepository extends JpaRepository<Note, Long> {

  List<Note> findByUserIdOrderByUpdatedAtDesc(Long userId);

  List<Note> findByUserIdAndSubjectIdOrderByUpdatedAtDesc(Long userId, Long subjectId);

  Page<Note> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);

  Page<Note> findByUserIdAndSubjectIdOrderByUpdatedAtDesc(Long userId, Long subjectId, Pageable pageable);

  @Query("""
      select n from Note n
      where n.userId = :userId
        and (:subjectId is null or n.subjectId = :subjectId)
        and (lower(n.title) like lower(concat('%', :q, '%'))
          or n.content like concat('%', :q, '%'))
      order by n.updatedAt desc
      """)
  List<Note> searchByTitleOrContent(
      @Param("userId") Long userId,
      @Param("subjectId") Long subjectId,
      @Param("q") String q
  );

  @Query("""
      select n from Note n
      where n.userId = :userId
        and (:subjectId is null or n.subjectId = :subjectId)
        and (lower(n.title) like lower(concat('%', :q, '%'))
          or n.content like concat('%', :q, '%'))
      order by n.updatedAt desc
      """)
  Page<Note> searchByTitleOrContent(
      @Param("userId") Long userId,
      @Param("subjectId") Long subjectId,
      @Param("q") String q,
      Pageable pageable
  );
}
