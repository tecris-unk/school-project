package com.school.school.controller;

import com.school.school.model.Student;
import com.school.school.service.StudentService;
import com.school.school.service.dto.StudentDto;
import com.school.school.service.mapper.StudentMapper;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
@AllArgsConstructor
public final class StudentController {

    private final StudentService service;

    private final StudentMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<StudentDto> findById(@PathVariable final Long id) {
        Student student = service.findStudentById(id);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDto(student));
    }

    @GetMapping(path = "/", params = "email")
    public ResponseEntity<StudentDto> getStudent(
            @RequestParam final String email) {
        Student student = service.findStudentByEmail(email);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDto(student));
    }

    @GetMapping("/")
    public ResponseEntity<List<StudentDto>> getAllStudents() {
        List<Student> students = service.findAllStudents();
        if (students.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        List<StudentDto> dtoList = students.stream()
                .map(mapper::toDto)
                .toList();
        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<Void> addStudent(
            @Valid @RequestBody final StudentDto studentDto) {
        service.createStudent(studentDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentDto> updateStudent(
            @PathVariable final Long id,
            @Valid @RequestBody final StudentDto updatedStudent) {
        Student student = service.updateStudent(id, updatedStudent);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDto(student));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable final Long id) {
        boolean isDeleted = service.deleteStudent(id);
        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.noContent().build();
    }
}
