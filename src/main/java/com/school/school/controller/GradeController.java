package com.school.school.controller;

import com.school.school.model.Grade;
import com.school.school.service.GradeService;
import com.school.school.service.dto.GradeDto;
import com.school.school.service.mapper.GradeMapper;
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

/**
 * Контроллер для оценок.
 */
@RestController
@RequestMapping("/api/grades")
@AllArgsConstructor
public final class GradeController {

    private final GradeService service;

    private final GradeMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<GradeDto> findById(@PathVariable final Long id) {
        Grade grade = service.findGradeById(id);

        if (grade == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDto(grade));
    }

    @GetMapping
    public ResponseEntity<List<GradeDto>> getAllGrades() {
        List<Grade> grades = service.findAllGrades();
        if (grades.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        List<GradeDto> dtoList = grades.stream()
                .map(mapper::toDto)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping
    public ResponseEntity<Void> addGrade(
            @Valid @RequestBody final GradeDto gradeDto) {
        Grade grade = service.createGrade(gradeDto);
        if (grade == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GradeDto> updateGrade(
            @PathVariable final Long id,
            @Valid @RequestBody final GradeDto gradeDto) {
        Grade grade = service.updateGrade(id, gradeDto);
        if (grade == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDto(grade));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGrade(@PathVariable final Long id) {
        boolean isDeleted = service.deleteGrade(id);
        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.noContent().build();
    }

}
