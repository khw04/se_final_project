package com.pokemo.notice.repository;

import com.pokemo.notice.domain.Notice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

  List<Notice> findAllByOrderByPinnedDescCreatedAtDesc();
}
