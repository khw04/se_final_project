package com.pokemo.note.service;

import com.pokemo.common.ApiException;
import com.pokemo.note.api.NotePatchRequest;
import com.pokemo.note.api.NoteRequest;
import com.pokemo.note.api.NoteResponse;
import com.pokemo.note.domain.Note;
import com.pokemo.note.repository.AttachmentRepository;
import com.pokemo.note.repository.NoteRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoteService {

  private final NoteRepository noteRepository;
  private final AttachmentRepository attachmentRepository;

  public NoteService(NoteRepository noteRepository, AttachmentRepository attachmentRepository) {
    this.noteRepository = noteRepository;
    this.attachmentRepository = attachmentRepository;
  }

  @Transactional(readOnly = true)
  public List<NoteResponse> getNotes(Long userId, Long subjectId, String q) {
    List<Note> notes = noteRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    if (subjectId != null) {
      notes = notes.stream().filter(n -> subjectId.equals(n.subjectId())).toList();
    }
    if (q != null && !q.isBlank()) {
      String needle = q.toLowerCase();
      notes = notes.stream()
          .filter(n -> n.title().toLowerCase().contains(needle)
              || n.content().toLowerCase().contains(needle))
          .toList();
    }
    return notes.stream().map(this::toResponse).toList();
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
    if (request.title() != null) note.updateTitle(request.title());
    if (request.subjectId() != null) note.updateSubject(request.subjectId());
    if (request.content() != null) note.updateContent(request.content());
    return toResponse(note);
  }

  @Transactional
  public void delete(Long userId, Long id) {
    Note note = findOwned(userId, id);
    attachmentRepository.findByNoteId(id).forEach(a -> attachmentRepository.delete(a));
    noteRepository.delete(note);
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
}
