package com.school.school.controller;

import com.school.school.model.Student;
import com.school.school.service.StudentService;
import com.school.school.service.dto.StudentDTO;
import com.school.school.service.mapper.StudentMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<StudentDTO> findById(@PathVariable final Long id) {
        Student student = service.findStudentById(id);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDTO(student));
    }

    /**
     *{@code GET} : найти учащегося по электронной почте.
     *
     * @param email электронная почта учащегося
     * @return учащийся, если он найден
     */
    @GetMapping("/by-email")
    public ResponseEntity<StudentDTO> findByEmail(
            @RequestParam final String email) {
        Student student = service.findStudentByEmail(email);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDTO(student));
    }

    /**
     * {@code GET} : получить всех учащихся.
     *
     * @return список всех пользователей, если он не пуст
     */
    @GetMapping()
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        List<Student> students = service.findAllStudents();
        if (students.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        List<StudentDTO> dtoList = students.stream()
                .map(mapper::toDTO)
                .toList();
        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }

    /**
     * {@code POST} : добавить нового учащегося.
     *
     * @param studentDTO учащийся, которого нужно добавить
     * @return учащегося, если его получилось добавить
     */
    @PostMapping
    public ResponseEntity<Void> addStudent(
            @RequestBody final StudentDTO studentDTO) {
        service.createStudent(studentDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * {@code PUT} : обновить учащегося.
     *
     * @param id индетификатор учащегося
     * @param updatedStudent обновление учащегося
     * @return обновленного учащегося, если это получилось сделать
     */
    @PutMapping("/{id}")
    public ResponseEntity<StudentDTO> updateStudent(
            @PathVariable final Long id,
            @RequestBody final StudentDTO updatedStudent) {
        Student student = service.updateStudent(id, updatedStudent);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDTO(student));
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
