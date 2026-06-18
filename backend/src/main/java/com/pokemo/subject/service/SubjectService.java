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

  /** 신규 사용자에게 제공하는 기본 과목 목록(이름, 색상). */
  private static final List<String[]> DEFAULT_SUBJECTS = List.of(
      new String[] {"미적분", "#EF4444"},
      new String[] {"자료구조", "#3B82F6"},
      new String[] {"영어회화", "#22C55E"},
      new String[] {"운영체제", "#F59E0B"},
      new String[] {"데이터베이스", "#8B5CF6"}
  );

  private final SubjectRepository subjectRepository;

  public SubjectService(SubjectRepository subjectRepository) {
    this.subjectRepository = subjectRepository;
  }

  @Transactional(readOnly = true)
  @Cacheable(value = "subjects", key = "#userId")
  public List<SubjectResponse> getAll(long userId) {
    return subjectRepository.findByUserIdOrderByIdAsc(userId).stream()
        .map(SubjectResponse::from)
        .toList();
  }

  @Transactional
  @CacheEvict(value = "subjects", key = "#userId")
  public SubjectResponse create(long userId, CreateSubjectRequest request) {
    if (subjectRepository.existsByUserIdAndName(userId, request.name())) {
      throw new ApiException(HttpStatus.CONFLICT, "이미 존재하는 과목명입니다.");
    }
    Subject subject = new Subject(userId, request.name(), request.color());
    return SubjectResponse.from(subjectRepository.save(subject));
  }

  @Transactional
  @CacheEvict(value = "subjects", key = "#userId")
  public SubjectResponse update(long userId, Long id, CreateSubjectRequest request) {
    Subject subject = findOwned(userId, id);
    subjectRepository.findByUserIdAndName(userId, request.name())
        .filter(existing -> !existing.id().equals(id))
        .ifPresent(existing -> {
          throw new ApiException(HttpStatus.CONFLICT, "이미 존재하는 과목명입니다.");
        });
    subject.update(request.name(), request.color());
    return SubjectResponse.from(subject);
  }

  @Transactional
  @CacheEvict(value = "subjects", key = "#userId")
  public void delete(long userId, Long id) {
    Subject subject = findOwned(userId, id);
    subjectRepository.delete(subject);
  }

  /** 가입 직후 사용자에게 기본 과목을 1회 생성한다. 이미 과목이 있으면 건너뛴다. */
  @Transactional
  @CacheEvict(value = "subjects", key = "#userId")
  public void seedDefaults(long userId) {
    if (subjectRepository.existsByUserId(userId)) {
      return;
    }
    for (String[] preset : DEFAULT_SUBJECTS) {
      subjectRepository.save(new Subject(userId, preset[0], preset[1]));
    }
  }

  private Subject findOwned(long userId, Long id) {
    Subject subject = subjectRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "과목을 찾을 수 없습니다."));
    if (!subject.userId().equals(userId)) {
      throw new ApiException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
    }
    return subject;
  }
}
