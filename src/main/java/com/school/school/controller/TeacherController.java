package com.school.school.controller;

import com.school.school.model.Teacher;
import com.school.school.service.TeacherService;
import com.school.school.service.dto.TeacherDto;
import com.school.school.service.mapper.TeacherMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Найти учителя по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Учитель найден"),
            @ApiResponse(responseCode = "404", description = "Учитель не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TeacherDto> findById(@PathVariable final Long id) {
        Teacher teacher = service.findTeacherById(id);
        return ResponseEntity.ok(mapper.toDto(teacher));
    }

    @Operation(summary = "Найти всех учителей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Учителя найдены"),
            @ApiResponse(responseCode = "404", description = "Учителя не найдены")
    })
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

    @Operation(summary = "Создать учителя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Учитель создан")
    })
    @PostMapping
    public ResponseEntity<Void> addTeacher(
            @RequestBody final TeacherDto teacherDto) {
        service.createTeacher(teacherDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Обновить учителя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Учитель обновлён"),
            @ApiResponse(responseCode = "404", description = "Учитель не найден")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TeacherDto> updateTeacher(
            @PathVariable final Long id,
            @RequestBody final TeacherDto teacherDto) {
        Teacher teacher = service.updateTeacher(id, teacherDto);
        return ResponseEntity.ok(mapper.toDto(teacher));
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
