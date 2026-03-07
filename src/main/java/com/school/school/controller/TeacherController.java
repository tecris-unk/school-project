package com.school.school.controller;

import com.school.school.service.TeacherService;
import com.school.school.service.dto.request.TeacherRequest;
import com.school.school.service.dto.response.TeacherResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/teachers")
@AllArgsConstructor
public class TeacherController {

    private final TeacherService service;

    @Operation(summary = "Найти учителя по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Учитель найден"),
            @ApiResponse(responseCode = "404", description = "Учитель не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TeacherResponse> findById(@PathVariable @Positive final Long id) {
        return ResponseEntity.ok(service.findTeacherById(id));
    }

    @Operation(summary = "Найти всех учителей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Учителя найдены"),
            @ApiResponse(responseCode = "204", description = "Список учителей пуст")
    })

    @GetMapping
    public ResponseEntity<List<TeacherResponse>> getAllTeachers() {
        List<TeacherResponse> teachers = service.findAllTeachers();
        if (teachers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(teachers);
    }

    @Operation(summary = "Создать учителя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Учитель создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
    })
    @PostMapping
    public ResponseEntity<TeacherResponse> addTeacher(
            @Valid @RequestBody final TeacherRequest teacherRequest) {
        TeacherResponse created = service.createTeacher(teacherRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Обновить учителя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Учитель обновлён"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Учитель не найден")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TeacherResponse> updateTeacher(
            @PathVariable @Positive final Long id,
            @Valid @RequestBody final TeacherRequest teacherRequest) {
        return ResponseEntity.ok(service.updateTeacher(id, teacherRequest));
    }

    @Operation(summary = "Удалить учителя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Учитель удалён"),
            @ApiResponse(responseCode = "404", description = "Учитель не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable @Positive final Long id) {
        service.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }
}
