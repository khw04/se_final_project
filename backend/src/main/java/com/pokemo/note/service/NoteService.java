package com.pokemo.note.service;

import com.pokemo.common.ApiException;
import com.pokemo.common.PageResponse;
import com.pokemo.note.api.CreateTagRequest;
import com.pokemo.note.api.NotePatchRequest;
import com.pokemo.note.api.NoteRequest;
import com.pokemo.note.api.NoteResponse;
import com.pokemo.note.api.NoteVersionResponse;
import com.pokemo.note.api.TagResponse;
import com.pokemo.note.domain.Note;
import com.pokemo.note.domain.Tag;
import com.pokemo.note.repository.AttachmentRepository;
import com.pokemo.note.repository.NoteRepository;
import com.pokemo.note.repository.NoteVersionRepository;
import com.pokemo.note.repository.TagRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoteService {

  private final NoteRepository noteRepository;
  private final AttachmentRepository attachmentRepository;
  private final TagRepository tagRepository;
  private final NoteVersionRepository noteVersionRepository;

  public NoteService(
      NoteRepository noteRepository,
      AttachmentRepository attachmentRepository,
      TagRepository tagRepository,
      NoteVersionRepository noteVersionRepository
  ) {
    this.noteRepository = noteRepository;
    this.attachmentRepository = attachmentRepository;
    this.tagRepository = tagRepository;
    this.noteVersionRepository = noteVersionRepository;
  }

  @Transactional(readOnly = true)
  public List<NoteResponse> getNotes(Long userId, Long subjectId, List<Long> tagIds, String q) {
    List<Note> baseNotes = subjectId == null
        ? noteRepository.findByUserIdOrderByUpdatedAtDesc(userId)
        : noteRepository.findByUserIdAndSubjectIdOrderByUpdatedAtDesc(userId, subjectId);
    List<Note> notes = baseNotes;
    if (tagIds != null && !tagIds.isEmpty()) {
      validateOwnedTags(userId, tagIds);
      notes = notes.stream().filter(n -> n.tagIds().containsAll(tagIds)).toList();
    }
    if (q != null && !q.isBlank()) {
      List<Note> textMatches = noteRepository.searchByTitleOrContent(userId, subjectId, q.strip());
      Set<Long> matchingTagIds = tagRepository.findByUserIdAndNameContainingIgnoreCase(userId, q.strip())
          .stream()
          .map(Tag::id)
          .collect(java.util.stream.Collectors.toSet());
      Set<Long> textMatchIds = textMatches.stream().map(Note::id).collect(java.util.stream.Collectors.toSet());
      notes = notes.stream()
          .filter(n -> textMatchIds.contains(n.id()) || n.tagIds().stream().anyMatch(matchingTagIds::contains))
          .toList();
    }
    return notes.stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public PageResponse<NoteResponse> getNotesPaged(
      Long userId,
      Long subjectId,
      List<Long> tagIds,
      String q,
      Pageable pageable
  ) {
    Page<Note> page;
    if (q != null && !q.isBlank()) {
      page = noteRepository.searchByTitleOrContent(userId, subjectId, q.strip(), pageable);
    } else {
      page = subjectId == null
          ? noteRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable)
          : noteRepository.findByUserIdAndSubjectIdOrderByUpdatedAtDesc(userId, subjectId, pageable);
    }
    if (tagIds != null && !tagIds.isEmpty()) {
      validateOwnedTags(userId, tagIds);
      List<NoteResponse> content = page.getContent().stream()
          .filter(note -> note.tagIds().containsAll(tagIds))
          .map(this::toResponse)
          .toList();
      return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(),
          page.getTotalPages(), page.isFirst(), page.isLast());
    }
    return PageResponse.from(page.map(this::toResponse));
  }

  @Transactional(readOnly = true)
  public NoteResponse getNote(Long userId, Long id) {
    return toResponse(findOwned(userId, id));
  }

  @Transactional
  public NoteResponse create(Long userId, NoteRequest request) {
    Note note = new Note(userId, request.title(), request.subjectId(),
        request.content() != null ? request.content() : "");
    return toResponse(noteRepository.save(note));
  }

  @Transactional
  public NoteResponse patch(Long userId, Long id, NotePatchRequest request) {
    Note note = findOwned(userId, id);
    saveVersion(note);
    if (request.title() != null) note.updateTitle(request.title());
    if (request.subjectId() != null) note.updateSubject(request.subjectId());
    if (request.content() != null) note.updateContent(request.content());
    return toResponse(note);
  }

  @Transactional
  public void delete(Long userId, Long id) {
    Note note = findOwned(userId, id);
    attachmentRepository.findByNoteId(id).forEach(a -> attachmentRepository.delete(a));
    noteVersionRepository.deleteByNoteId(id);
    noteRepository.delete(note);
  }

  @Transactional(readOnly = true)
  public List<NoteVersionResponse> getVersions(Long userId, Long noteId) {
    findOwned(userId, noteId);
    return noteVersionRepository.findByNoteIdAndUserIdOrderByVersionNumberDesc(noteId, userId).stream()
        .map(NoteVersionResponse::from)
        .toList();
  }

  @Transactional
  public NoteResponse restoreVersion(Long userId, Long noteId, Long versionId) {
    Note note = findOwned(userId, noteId);
    var version = noteVersionRepository.findByIdAndNoteIdAndUserId(versionId, noteId, userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "노트 버전을 찾을 수 없습니다."));
    saveVersion(note);
    note.update(version.title(), version.subjectId(), version.content());
    return toResponse(note);
  }

  @Transactional
  public NoteResponse addTag(Long userId, Long noteId, Long tagId) {
    Note note = findOwned(userId, noteId);
    findOwnedTag(userId, tagId);
    note.addTag(tagId);
    return toResponse(note);
  }

  @Transactional
  public NoteResponse removeTag(Long userId, Long noteId, Long tagId) {
    Note note = findOwned(userId, noteId);
    findOwnedTag(userId, tagId);
    note.removeTag(tagId);
    return toResponse(note);
  }

  @Transactional(readOnly = true)
  public List<TagResponse> getTags(Long userId) {
    return tagRepository.findByUserIdOrderByNameAsc(userId).stream()
        .map(TagResponse::from)
        .toList();
  }

  @Transactional
  public TagResponse createTag(Long userId, CreateTagRequest request) {
    String name = request.name().strip();
    if (tagRepository.existsByUserIdAndName(userId, name)) {
      throw new ApiException(HttpStatus.CONFLICT, "이미 존재하는 태그입니다.");
    }
    Tag tag = new Tag(userId, name);
    return TagResponse.from(tagRepository.save(tag));
  }

  @Transactional
  public void deleteTag(Long userId, Long tagId) {
    findOwnedTag(userId, tagId);
    noteRepository.findByUserIdOrderByUpdatedAtDesc(userId)
        .forEach(note -> note.removeTag(tagId));
    tagRepository.deleteById(tagId);
  }

  private NoteResponse toResponse(Note note) {
    List<Long> attachmentIds = attachmentRepository.findByNoteId(note.id())
        .stream().map(a -> a.id()).toList();
    return NoteResponse.from(note, attachmentIds);
  }

  private Note findOwned(Long userId, Long id) {
    Note note = noteRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "노트를 찾을 수 없습니다."));
    if (!note.userId().equals(userId)) {
      throw new ApiException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
    }
    return note;
  }

  private void saveVersion(Note note) {
    int nextVersion = noteVersionRepository.countByNoteId(note.id()) + 1;
    noteVersionRepository.save(new com.pokemo.note.domain.NoteVersion(
        note.id(),
        note.userId(),
        nextVersion,
        note.title(),
        note.subjectId(),
        note.content()));
  }

  private Tag findOwnedTag(Long userId, Long tagId) {
    return tagRepository.findByIdAndUserId(tagId, userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "태그를 찾을 수 없습니다."));
  }

  private void validateOwnedTags(Long userId, List<Long> tagIds) {
    Set<Long> uniqueTagIds = new HashSet<>(tagIds);
    if (tagRepository.findByUserIdAndIdIn(userId, uniqueTagIds).size() != uniqueTagIds.size()) {
      throw new ApiException(HttpStatus.NOT_FOUND, "태그를 찾을 수 없습니다.");
    }
  }
}
