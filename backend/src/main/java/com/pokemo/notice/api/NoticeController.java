package com.pokemo.notice.api;

import com.pokemo.common.PageResponse;
import com.pokemo.notice.service.NoticeService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

  private final NoticeService noticeService;

  public NoticeController(NoticeService noticeService) {
    this.noticeService = noticeService;
  }

  @GetMapping
  List<NoticeResponse> list() {
    return noticeService.list();
  }

  @GetMapping("/paged")
  PageResponse<NoticeResponse> listPaged(
      @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
      @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size
  ) {
    Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)));
    return noticeService.listPaged(pageable);
  }

  @GetMapping("/{id}")
  NoticeResponse detail(@PathVariable long id) {
    return noticeService.detail(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  NoticeResponse create(@Valid @RequestBody NoticeRequest request, Principal principal) {
    return noticeService.create(request, principal.getName());
  }

  @PutMapping("/{id}")
  NoticeResponse update(@PathVariable long id, @Valid @RequestBody NoticeRequest request) {
    return noticeService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(@PathVariable long id) {
    noticeService.delete(id);
  }
}
