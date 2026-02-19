package com.school.school.controller;

import com.school.school.model.SchoolClass;
import com.school.school.service.SchoolClassService;
import com.school.school.service.dto.SchoolClassDTO;
import com.school.school.service.mapper.SchoolClassMapper;
import java.util.List;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classes")
@AllArgsConstructor
public final class SchoolClassController {

    private final SchoolClassService service;

    private final SchoolClassMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<SchoolClassDTO> findById(
            @PathVariable final Long id) {
        SchoolClass schoolClass = service.findClassById(id);
        if (schoolClass == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDTO(schoolClass));
    }

    @GetMapping
    public ResponseEntity<List<SchoolClassDTO>> getAllClasses() {
        List<SchoolClass> classes = service.findAllClasses();
        if (classes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        List<SchoolClassDTO> dtoList = classes.stream()
                .map(mapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping
    public ResponseEntity<Void> addClass(
           @Valid @RequestBody final SchoolClassDTO classDTO) {
        service.createClass(classDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SchoolClassDTO> updateClass(
            @PathVariable final Long id,
            @Valid @RequestBody final SchoolClassDTO classDTO) {
        SchoolClass schoolClass = service.updateClass(id, classDTO);
        if (schoolClass == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDTO(schoolClass));
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
