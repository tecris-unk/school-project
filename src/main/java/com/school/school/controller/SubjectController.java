package com.school.school.controller;

import com.school.school.model.Subject;
import com.school.school.service.SubjectService;
import com.school.school.service.dto.SubjectDto;
import com.school.school.service.mapper.SubjectMapper;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subjects")
@AllArgsConstructor
public final class SubjectController {

    private final SubjectService service;

    private final SubjectMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<SubjectDto> findById(@PathVariable final Long id) {
        Subject subject = service.findSubjectById(id);
        if (subject == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDto(subject));
    }

    @GetMapping
    public ResponseEntity<List<SubjectDto>> getAllSubjects() {
        List<Subject> subjects = service.findAllSubjects();
        if (subjects.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        List<SubjectDto> dtoList = subjects.stream()
                .map(mapper::toDto)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping
    public ResponseEntity<Void> addSubject(
            @Valid @RequestBody final SubjectDto subjectDto) {
        Subject subject = service.createSubject(subjectDto);
        if (subject == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubjectDto> updateSubject(
            @PathVariable final Long id,
            @Valid @RequestBody final SubjectDto subjectDto) {
        Subject subject = service.updateSubject(id, subjectDto);
        if (subject == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDto(subject));
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
