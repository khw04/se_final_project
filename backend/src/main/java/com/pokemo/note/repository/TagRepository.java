package com.pokemo.note.repository;

import com.pokemo.note.domain.Tag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

  List<Tag> findByUserId(Long userId);

  boolean existsByUserIdAndName(Long userId, String name);
}
