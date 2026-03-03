package com.school.school.controller;

import com.school.school.service.StudentService;
import com.school.school.service.dto.request.StudentRequest;
import com.school.school.service.dto.request.StudentWithGradesRequest;
import com.school.school.service.dto.response.StudentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
    public ResponseEntity<StudentResponse> findById(@PathVariable final Long id) {
        return ResponseEntity.ok(service.findStudentById(id));
    }

    @Operation(summary = "Получить всех учеников (опционально поиск по почте и дате оценок)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ученики найдены"),
            @ApiResponse(responseCode = "204", description = "Список учеников пуст")
    })
    @GetMapping
    public ResponseEntity<Page<StudentResponse>> getAllStudents(
            @RequestParam(required = false) @Email String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<StudentResponse> page = service.findStudentsByEmailAndDate(email, date, pageable);

        if (page.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Создать ученика (опционально с оценками)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ученик создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
    })
    @PostMapping
    public ResponseEntity<StudentResponse> addStudent(
            @Valid @RequestBody final StudentRequest studentRequest) {
        StudentResponse created = service.createStudent(studentRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Создать ученика с оценками")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ученик создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
    })
    @PostMapping("/with_gradesT")
    public ResponseEntity<StudentResponse> addStudentWithGradesTransactional(
            @Valid @RequestBody final StudentWithGradesRequest request) {
        StudentResponse created = service.createStudentWithGradesTransactional(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Создать ученика с оценками, без транзакционал")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ученик создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
    })
    @PostMapping("/with_grades")
    public ResponseEntity<StudentResponse> addStudentWithGradesNoTransactional(
            @Valid @RequestBody final StudentWithGradesRequest request) {
        StudentResponse created = service.createStudentWithGradesNoTransactional(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Обновить ученика")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ученик обновлён"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Ученик не найден")
    })
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable final Long id,
            @Valid @RequestBody final StudentRequest updatedStudent) {
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
