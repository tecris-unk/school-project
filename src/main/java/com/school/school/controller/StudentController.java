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

/**
 * Контроллер для учащегося.
 */
@RestController
@RequestMapping("/api/students")
@AllArgsConstructor
public final class StudentController {

    private final StudentService service;

    private final StudentMapper mapper;

    /**
     * {@code GET} : найти учащегося по айди.
     *
     * @param id индетификатор учащегося
     * @return учащийся, если он найден
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentDto> findById(@PathVariable final Long id) {
        Student student = service.findStudentById(id);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDto(student));
    }

    /**
     * {@code GET} : найти учащегося по электронной почте.
     *
     * @param email электронная почта учащегося
     * @return учащийся, если он найден
     */
    @GetMapping("/by-email")
    public ResponseEntity<StudentDto> findByEmail(
            @RequestParam final String email) {
        Student student = service.findStudentByEmail(email);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDto(student));
    }

    /**
     * {@code GET} : получить всех учащихся.
     *
     * @return список всех пользователей, если он не пуст
     */
    @GetMapping()
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

    /**
     * {@code GET} : получить всех учащихся с загруженными предметами.
     *
     * @return список всех пользователей с предзагруженными предметами
     */
    @GetMapping("/with-subjects")
    public ResponseEntity<List<StudentDto>> getAllStudentsWithSubjects() {
        List<Student> students = service.findAllStudentsWithSubjects();
        if (students.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        List<StudentDto> dtoList = students.stream()
                .map(mapper::toDto)
                .toList();
        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }

    /**
     * {@code POST} : добавить нового учащегося.
     *
     * @param studentDto учащийся, которого нужно добавить
     * @return учащегося, если его получилось добавить
     */
    @PostMapping
    public ResponseEntity<Void> addStudent(
            @Valid @RequestBody final StudentDto studentDto) {
        service.createStudent(studentDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * {@code PUT} : обновить учащегося.
     *
     * @param id             индетификатор учащегося
     * @param updatedStudent обновление учащегося
     * @return обновленного учащегося, если это получилось сделать
     */
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

    /**
     * {@code DELETE} : удалить учащегося.
     *
     * @param id индетификатор учащегося
     * @return получилось ли удалить учащегося
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable final Long id) {
        boolean isDeleted = service.deleteStudent(id);
        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.noContent().build();
    }
}
