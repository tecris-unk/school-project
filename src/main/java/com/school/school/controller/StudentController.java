package com.school.school.controller;

import com.school.school.service.StudentSearchQueryType;
import com.school.school.service.StudentService;
import com.school.school.service.dto.request.StudentRequest;
import com.school.school.service.dto.request.StudentWithGradesRequest;
import com.school.school.service.dto.response.StudentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    @Operation(summary = "Сложный поиск учеников по вложенным сущностям")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ученики найдены"),
            @ApiResponse(responseCode = "204", description = "Список учеников пуст")
    })
    @GetMapping
    public ResponseEntity<Page<StudentResponse>> searchStudents(
            @RequestParam(required = false) @Email String teacherEmail,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(defaultValue = "NATIVE") StudentSearchQueryType queryType,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        String normalizedTeacherEmail = normalizeTextParam(teacherEmail);
        String normalizedSubjectName = normalizeTextParam(subjectName);

        Page<StudentResponse> page = service.findStudentsByNestedFilters(
                normalizedTeacherEmail,
                normalizedSubjectName,
                minScore,
                pageable,
                queryType
        );
        return ResponseEntity.ok(page);
    }

    private String normalizeTextParam(final String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    @Operation(summary = "Создать ученика")
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
