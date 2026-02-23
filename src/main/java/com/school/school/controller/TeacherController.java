package com.school.school.controller;

import com.school.school.model.Teacher;
import com.school.school.service.TeacherService;
import com.school.school.service.dto.TeacherDto;
import com.school.school.service.mapper.TeacherMapper;
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
@RequestMapping("/api/teachers")
@AllArgsConstructor
public final class TeacherController {

    private final TeacherService service;

    private final TeacherMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<TeacherDto> findById(@PathVariable final Long id) {
        Teacher teacher = service.findTeacherById(id);
        if (teacher == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDto(teacher));
    }

    @GetMapping
    public ResponseEntity<List<TeacherDto>> getAllTeachers() {
        List<Teacher> teachers = service.findAllTeachers();
        if (teachers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        List<TeacherDto> dtoList = teachers.stream()
                .map(mapper::toDto)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping
    public ResponseEntity<Void> addTeacher(
            @RequestBody final TeacherDto teacherDto) {
        service.createTeacher(teacherDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherDto> updateTeacher(
            @PathVariable final Long id,
            @RequestBody final TeacherDto teacherDto) {
        Teacher teacher = service.updateTeacher(id, teacherDto);
        if (teacher == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDto(teacher));
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
