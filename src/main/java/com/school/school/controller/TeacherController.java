package com.school.school.controller;

import com.school.school.service.TeacherService;
import com.school.school.service.dto.TeacherDto;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teachers")
@AllArgsConstructor
public final class TeacherController {

    private final TeacherService service;

    @Operation(summary = "Найти учителя по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Учитель найден"),
            @ApiResponse(responseCode = "404", description = "Учитель не найден")
    })

    @GetMapping("/{id}")
    public ResponseEntity<TeacherDto> findById(@PathVariable final Long id) {
        return ResponseEntity.ok(service.findTeacherById(id));
    }

    @Operation(summary = "Найти всех учителей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Учителя найдены"),
            @ApiResponse(responseCode = "204", description = "Список учителей пуст")
    })

    @GetMapping
    public ResponseEntity<List<TeacherDto>> getAllTeachers() {
        List<TeacherDto> teachers = service.findAllTeachers();
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
    public ResponseEntity<TeacherDto> addTeacher(
            @Valid @RequestBody final TeacherDto teacherDto) {
        TeacherDto created = service.createTeacher(teacherDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Обновить учителя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Учитель обновлён"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Учитель не найден")
    })

    @PutMapping("/{id}")
    public ResponseEntity<TeacherDto> updateTeacher(
            @PathVariable final Long id,
           @Valid @RequestBody final TeacherDto teacherDto) {
        return ResponseEntity.ok(service.updateTeacher(id, teacherDto));
    }

    @Operation(summary = "Удалить учителя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Учитель удалён"),
            @ApiResponse(responseCode = "404", description = "Учитель не найден")
    })

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable final Long id) {
        service.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }
}
