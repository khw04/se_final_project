package com.pokemo.subject.api;

import com.pokemo.common.CurrentUserProvider;
import com.pokemo.subject.service.SubjectService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
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
@RequestMapping("/api/subjects")
public class SubjectController {

  private final SubjectService subjectService;
  private final CurrentUserProvider currentUserProvider;

  public SubjectController(SubjectService subjectService, CurrentUserProvider currentUserProvider) {
    this.subjectService = subjectService;
    this.currentUserProvider = currentUserProvider;
  }

  @GetMapping
  List<SubjectResponse> getAll(Principal principal) {
    return subjectService.getAll(currentUserProvider.userId(principal));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  SubjectResponse create(Principal principal, @Valid @RequestBody CreateSubjectRequest request) {
    return subjectService.create(currentUserProvider.userId(principal), request);
  }

  @PutMapping("/{id}")
  SubjectResponse update(
      Principal principal,
      @PathVariable Long id,
      @Valid @RequestBody CreateSubjectRequest request) {
    return subjectService.update(currentUserProvider.userId(principal), id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(Principal principal, @PathVariable Long id) {
    subjectService.delete(currentUserProvider.userId(principal), id);
  }
}
