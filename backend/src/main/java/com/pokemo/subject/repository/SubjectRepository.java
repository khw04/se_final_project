package com.pokemo.subject.repository;

import com.pokemo.subject.domain.Subject;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

  boolean existsByName(String name);

  Optional<Subject> findByName(String name);
}
