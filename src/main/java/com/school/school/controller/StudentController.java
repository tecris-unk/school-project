package com.school.school.controller;

import com.school.school.model.Student;
import com.school.school.service.StudentService;
import com.school.school.service.dto.StudentDTO;
import com.school.school.service.mapper.StudentMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@AllArgsConstructor
public final class StudentController {

    private final StudentService service;

    private final StudentMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<@NonNull StudentDTO> findById(
            @PathVariable final Long id
    ) {
        Student student = service.findStudentById(id);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toDTO(student));
    }

    @GetMapping()
    public ResponseEntity<@NonNull List<StudentDTO>> getAllStudents() {
        List<Student> students = service.findAllStudents();
        if (students.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<StudentDTO> dtoList = students.stream()
                .map(mapper::toDTO)
                .toList();
        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }

    @GetMapping("/by-email")
    public ResponseEntity<StudentDTO> findByEmail(
            @RequestParam final String email
    ) {
        Student student = service.findStudentByEmail(email);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDTO(student));
    }

    @PostMapping
    public ResponseEntity<@NonNull StudentDTO> addStudent(
            @RequestBody final StudentDTO studentDTO
    ) {
        Student student = service.createStudent(studentDTO);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
        return ResponseEntity.ok(mapper.toDTO(student));
    }

    @PutMapping("/{id}")
    public ResponseEntity<@NonNull StudentDTO> updateStudent(
            @PathVariable final Long id,
            @RequestBody final StudentDTO updatedStudent) {
        Student student = service.updateStudent(id, updatedStudent);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toDTO(student));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable final Long id) {
        boolean isDeleted = service.deleteStudent(id);
        if (!isDeleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
