package com.pokemo.note.api;

import com.pokemo.common.CurrentUserProvider;
import com.pokemo.note.service.NoteService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NoteController {

  private final NoteService noteService;
  private final CurrentUserProvider currentUserProvider;

  public NoteController(NoteService noteService, CurrentUserProvider currentUserProvider) {
    this.noteService = noteService;
    this.currentUserProvider = currentUserProvider;
  }

  @GetMapping("/notes")
  List<NoteResponse> getNotes(
      Principal principal,
      @RequestParam(required = false) Long subjectId,
      @RequestParam(required = false) List<Long> tagIds,
      @RequestParam(required = false) String q
  ) {
    long userId = currentUserProvider.userId(principal);
    return noteService.getNotes(userId, subjectId, tagIds, q);
  }

  @GetMapping("/notes/{id}")
  NoteResponse getNote(Principal principal, @PathVariable Long id) {
    long userId = currentUserProvider.userId(principal);
    return noteService.getNote(userId, id);
  }

  @PostMapping("/notes")
  @ResponseStatus(HttpStatus.CREATED)
  NoteResponse create(Principal principal, @Valid @RequestBody NoteRequest request) {
    long userId = currentUserProvider.userId(principal);
    return noteService.create(userId, request);
  }

  @PatchMapping("/notes/{id}")
  NoteResponse patch(
      Principal principal,
      @PathVariable Long id,
      @RequestBody NotePatchRequest request
  ) {
    long userId = currentUserProvider.userId(principal);
    return noteService.patch(userId, id, request);
  }

  @DeleteMapping("/notes/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(Principal principal, @PathVariable Long id) {
    long userId = currentUserProvider.userId(principal);
    noteService.delete(userId, id);
  }

  @PostMapping("/notes/{noteId}/tags/{tagId}")
  NoteResponse addTag(Principal principal, @PathVariable Long noteId, @PathVariable Long tagId) {
    long userId = currentUserProvider.userId(principal);
    return noteService.addTag(userId, noteId, tagId);
  }

  @DeleteMapping("/notes/{noteId}/tags/{tagId}")
  NoteResponse removeTag(Principal principal, @PathVariable Long noteId, @PathVariable Long tagId) {
    long userId = currentUserProvider.userId(principal);
    return noteService.removeTag(userId, noteId, tagId);
  }

  @GetMapping("/tags")
  List<TagResponse> getTags(Principal principal) {
    long userId = currentUserProvider.userId(principal);
    return noteService.getTags(userId);
  }

  @PostMapping("/tags")
  @ResponseStatus(HttpStatus.CREATED)
  TagResponse createTag(Principal principal, @Valid @RequestBody CreateTagRequest request) {
    long userId = currentUserProvider.userId(principal);
    return noteService.createTag(userId, request);
  }
}
