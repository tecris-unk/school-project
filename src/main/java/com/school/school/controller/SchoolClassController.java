package com.school.school.controller;

import com.school.school.model.SchoolClass;
import com.school.school.service.SchoolClassService;
import com.school.school.service.dto.SchoolClassDto;
import com.school.school.service.mapper.SchoolClassMapper;
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
 * Контроллер для класса.
 */
@RestController
@RequestMapping("/api/classes")
@AllArgsConstructor
public final class SchoolClassController {

    private final SchoolClassService service;

    private final SchoolClassMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<SchoolClassDto> findById(
            @PathVariable final Long id) {
        SchoolClass schoolClass = service.findClassById(id);
        if (schoolClass == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDto(schoolClass));
    }

    @GetMapping
    public ResponseEntity<List<SchoolClassDto>> getAllClasses() {
        List<SchoolClass> classes = service.findAllClasses();
        if (classes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        List<SchoolClassDto> dtoList = classes.stream()
                .map(mapper::toDto)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping
    public ResponseEntity<Void> addClass(
            @Valid @RequestBody final SchoolClassDto classDto) {
        service.createClass(classDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SchoolClassDto> updateClass(
            @PathVariable final Long id,
            @Valid @RequestBody final SchoolClassDto classDto) {
        SchoolClass schoolClass = service.updateClass(id, classDto);
        if (schoolClass == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDto(schoolClass));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable final Long id) {
        boolean isDeleted = service.deleteClass(id);
        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.noContent().build();
    }
}
