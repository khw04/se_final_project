package com.pokemo.subject.service;

import com.pokemo.common.ApiException;
import com.pokemo.subject.api.CreateSubjectRequest;
import com.pokemo.subject.api.SubjectResponse;
import com.pokemo.subject.domain.Subject;
import com.pokemo.subject.repository.SubjectRepository;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubjectService {

  private final SubjectRepository subjectRepository;

  public SubjectService(SubjectRepository subjectRepository) {
    this.subjectRepository = subjectRepository;
  }

  @Transactional(readOnly = true)
  @Cacheable("subjects")
  public List<SubjectResponse> getAll() {
    return subjectRepository.findAll().stream()
        .map(SubjectResponse::from)
        .toList();
  }

  @Transactional
  @CacheEvict(value = "subjects", allEntries = true)
  public SubjectResponse create(CreateSubjectRequest request) {
    if (subjectRepository.existsByName(request.name())) {
      throw new ApiException(HttpStatus.CONFLICT, "이미 존재하는 과목명입니다.");
    }
    Subject subject = new Subject(request.name(), request.color());
    return SubjectResponse.from(subjectRepository.save(subject));
  }

  @Transactional
  @CacheEvict(value = "subjects", allEntries = true)
  public SubjectResponse update(Long id, CreateSubjectRequest request) {
    Subject subject = subjectRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "과목을 찾을 수 없습니다."));
    subjectRepository.findByName(request.name())
        .filter(existing -> !existing.id().equals(id))
        .ifPresent(existing -> {
          throw new ApiException(HttpStatus.CONFLICT, "이미 존재하는 과목명입니다.");
        });
    subject.update(request.name(), request.color());
    return SubjectResponse.from(subject);
  }

  @Transactional
  @CacheEvict(value = "subjects", allEntries = true)
  public void delete(Long id) {
    if (!subjectRepository.existsById(id)) {
      throw new ApiException(HttpStatus.NOT_FOUND, "과목을 찾을 수 없습니다.");
    }
    subjectRepository.deleteById(id);
  }
}
