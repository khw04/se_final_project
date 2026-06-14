package com.pokemo.note.repository;

import com.pokemo.note.domain.Tag;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

  List<Tag> findByUserIdOrderByNameAsc(Long userId);

  Optional<Tag> findByIdAndUserId(Long id, Long userId);

  List<Tag> findByUserIdAndIdIn(Long userId, Collection<Long> ids);

  List<Tag> findByUserIdAndNameContainingIgnoreCase(Long userId, String name);

  boolean existsByUserIdAndName(Long userId, String name);
}
