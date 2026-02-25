package com.school.school.controller;

import com.school.school.service.StudentService;
import com.school.school.service.dto.GradeDto;
import com.school.school.service.dto.StudentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Получить ученика по id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ученик найден"),
            @ApiResponse(responseCode = "404", description = "Ученик не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<StudentDto> findById(@PathVariable final Long id) {
        return ResponseEntity.ok(service.findStudentById(id));
    }

    @Operation(summary = "Получить ученика по email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ученик найден"),
            @ApiResponse(responseCode = "404", description = "Ученик не найден")
    })
    @GetMapping(path = "/", params = "email")
    public ResponseEntity<StudentDto> findByEmail(
            @RequestParam(required = false) final String email) {
        return ResponseEntity.ok(service.findStudentByEmail(email));
    }

    @Operation(summary = "Получить всех учеников")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ученики найдены"),
            @ApiResponse(responseCode = "204", description = "Ученики не найдены")
    })
    @GetMapping("/")
    public ResponseEntity<List<StudentDto>> getAllStudents() {
        List<StudentDto> students = service.findAllStudents();
        if (students.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return new ResponseEntity<>(students, HttpStatus.OK);
    }

    @Operation(summary = "Создать ученика")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ученик создан")
    })
    @PostMapping("/")
    public ResponseEntity<StudentDto> addStudent(
            @Valid @RequestBody final StudentDto studentDto) {
        StudentDto created = service.createStudent(studentDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Создать ученика с оценками")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ученик создан")
    })
    @PostMapping("/")
    public ResponseEntity<Void> addStudentWithGrades(
            @Valid @RequestBody final StudentDto studentDto, final List<GradeDto> gradesDto) {
        service.createStudentWithGrades(studentDto, gradesDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Обновить ученика")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ученик обновлён"),
            @ApiResponse(responseCode = "404", description = "Ученик не найден")
    })
    @PutMapping("/{id}")
    public ResponseEntity<StudentDto> updateStudent(
            @PathVariable final Long id,
            @Valid @RequestBody final StudentDto updatedStudent) {
        return ResponseEntity.ok(service.updateStudent(id, updatedStudent));
    }

    @Operation(summary = "Удалить ученика")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ученик удалён"),
            @ApiResponse(responseCode = "404", description = "Ученик не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable final Long id) {
        service.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}
