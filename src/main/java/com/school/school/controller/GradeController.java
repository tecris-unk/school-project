package com.school.school.controller;

import com.school.school.model.Grade;
import com.school.school.service.GradeService;
import com.school.school.service.dto.GradeDTO;
import com.school.school.service.mapper.GradeMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grades")
@AllArgsConstructor
public final class GradeController {

    private final GradeService service;

    private final GradeMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<GradeDTO> findById(@PathVariable final Long id) {
        Grade grade = service.findGradeById(id);
        if (grade == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDTO(grade));
    }

    @GetMapping
    public ResponseEntity<List<GradeDTO>> getAllGrades() {
        List<Grade> grades = service.findAllGrades();
        if (grades.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        List<GradeDTO> dtoList = grades.stream()
                .map(mapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping
    public ResponseEntity<Void> addGrade(@RequestBody final GradeDTO gradeDTO) {
        Grade grade = service.createGrade(gradeDTO);
        if (grade == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GradeDTO> updateGrade(
            @PathVariable final Long id,
            @RequestBody final GradeDTO gradeDTO) {
        Grade grade = service.updateGrade(id, gradeDTO);
        if (grade == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDTO(grade));
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
