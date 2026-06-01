package com.pokemo.subject.seed;

import com.pokemo.subject.domain.Subject;
import com.pokemo.subject.repository.SubjectRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!prod")
public class SubjectSeedRunner implements ApplicationRunner {

  private final SubjectRepository subjectRepository;

  public SubjectSeedRunner(SubjectRepository subjectRepository) {
    this.subjectRepository = subjectRepository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    seed("미적분", "#EF4444");
    seed("자료구조", "#3B82F6");
    seed("영어회화", "#22C55E");
    seed("운영체제", "#F59E0B");
    seed("데이터베이스", "#8B5CF6");
  }

  private void seed(String name, String color) {
    if (!subjectRepository.existsByName(name)) {
      subjectRepository.save(new Subject(name, color));
    }
  }
}
