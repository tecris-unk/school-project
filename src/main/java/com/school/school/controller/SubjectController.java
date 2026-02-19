package com.school.school.controller;

import com.school.school.model.Subject;
import com.school.school.service.SubjectService;
import com.school.school.service.dto.SubjectDTO;
import com.school.school.service.mapper.SubjectMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subjects")
@AllArgsConstructor
public final class SubjectController {

    private final SubjectService service;

    private final SubjectMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<SubjectDTO> findById(@PathVariable final Long id) {
        Subject subject = service.findSubjectById(id);
        if (subject == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDTO(subject));
    }

    @GetMapping
    public ResponseEntity<List<SubjectDTO>> getAllSubjects() {
        List<Subject> subjects = service.findAllSubjects();
        if (subjects.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        List<SubjectDTO> dtoList = subjects.stream()
                .map(mapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping
    public ResponseEntity<Void> addSubject(
            @RequestBody final SubjectDTO subjectDTO) {
        Subject subject = service.createSubject(subjectDTO);
        if (subject == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubjectDTO> updateSubject(
            @PathVariable final Long id,
            @RequestBody final SubjectDTO subjectDTO) {
        Subject subject = service.updateSubject(id, subjectDTO);
        if (subject == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDTO(subject));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable final Long id) {
        boolean isDeleted = service.deleteSubject(id);
        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.noContent().build();
    }
}
