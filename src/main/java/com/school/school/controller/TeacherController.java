package com.school.school.controller;

import com.school.school.model.Teacher;
import com.school.school.service.TeacherService;
import com.school.school.service.dto.TeacherDTO;
import com.school.school.service.mapper.TeacherMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teachers")
@AllArgsConstructor
public final class TeacherController {

    private final TeacherService service;

    private final TeacherMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<TeacherDTO> findById(@PathVariable final Long id) {
        Teacher teacher = service.findTeacherById(id);
        if (teacher == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDTO(teacher));
    }

    @GetMapping
    public ResponseEntity<List<TeacherDTO>> getAllTeachers() {
        List<Teacher> teachers = service.findAllTeachers();
        if (teachers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        List<TeacherDTO> dtoList = teachers.stream()
                .map(mapper::toDTO)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping
    public ResponseEntity<Void> addTeacher(
            @RequestBody final TeacherDTO teacherDTO) {
        service.createTeacher(teacherDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherDTO> updateTeacher(
            @PathVariable final Long id,
            @RequestBody final TeacherDTO teacherDTO) {
        Teacher teacher = service.updateTeacher(id, teacherDTO);
        if (teacher == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDTO(teacher));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable final Long id) {
        boolean isDeleted = service.deleteTeacher(id);
        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.noContent().build();
    }
}
