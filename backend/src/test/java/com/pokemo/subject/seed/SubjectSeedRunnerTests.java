package com.pokemo.subject.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.pokemo.subject.repository.SubjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SubjectSeedRunnerTests {

  @Autowired
  private SubjectRepository subjectRepository;

  @Autowired
  private SubjectSeedRunner subjectSeedRunner;

  @Test
  void createsDefaultSubjectsUnderTestProfile() {
    assertThat(subjectRepository.findByName("미적분")).isPresent();
    assertThat(subjectRepository.findByName("자료구조")).isPresent();
    assertThat(subjectRepository.findByName("영어회화")).isPresent();
    assertThat(subjectRepository.findByName("운영체제")).isPresent();
    assertThat(subjectRepository.findByName("데이터베이스")).isPresent();
  }

  @Test
  void seedRunnerIsIdempotent() throws Exception {
    long count = subjectRepository.count();

    subjectSeedRunner.run(null);

    assertThat(subjectRepository.count()).isEqualTo(count);
  }
}
