package com.pokemo.notice.service;

import com.pokemo.common.ApiException;
import com.pokemo.common.PageResponse;
import com.pokemo.notice.api.NoticeRequest;
import com.pokemo.notice.api.NoticeResponse;
import com.pokemo.notice.domain.Notice;
import com.pokemo.notice.repository.NoticeRepository;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoticeService {

  private final NoticeRepository noticeRepository;

  public NoticeService(NoticeRepository noticeRepository) {
    this.noticeRepository = noticeRepository;
  }

  @Transactional(readOnly = true)
  @Cacheable("notices")
  public List<NoticeResponse> list() {
    return noticeRepository.findAllByOrderByPinnedDescCreatedAtDesc().stream()
        .map(NoticeResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public PageResponse<NoticeResponse> listPaged(Pageable pageable) {
    return PageResponse.from(noticeRepository.findAllByOrderByPinnedDescCreatedAtDesc(pageable)
        .map(NoticeResponse::from));
  }

  @Transactional
  public NoticeResponse detail(long id) {
    Notice notice = findById(id);
    notice.increaseViewCount();
    return NoticeResponse.from(notice);
  }

  @Transactional
  @CacheEvict(value = "notices", allEntries = true)
  public NoticeResponse create(NoticeRequest request, String author) {
    Notice notice = new Notice(request.title(), request.body(), request.tag(), request.pinned(), author);
    return NoticeResponse.from(noticeRepository.save(notice));
  }

  @Transactional
  @CacheEvict(value = "notices", allEntries = true)
  public NoticeResponse update(long id, NoticeRequest request) {
    Notice notice = findById(id);
    notice.update(request.title(), request.body(), request.tag(), request.pinned());
    return NoticeResponse.from(notice);
  }

  @Transactional
  @CacheEvict(value = "notices", allEntries = true)
  public void delete(long id) {
    Notice notice = findById(id);
    noticeRepository.delete(notice);
  }

  private Notice findById(long id) {
    return noticeRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notice not found"));
  }
}
