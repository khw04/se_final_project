package com.pokemo.subject.repository;

import com.pokemo.subject.domain.Subject;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

  List<Subject> findByUserIdOrderByIdAsc(Long userId);

  boolean existsByUserId(Long userId);

  boolean existsByUserIdAndName(Long userId, String name);

  Optional<Subject> findByUserIdAndName(Long userId, String name);
}
