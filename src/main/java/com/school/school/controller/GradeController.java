package com.school.school.controller;

import com.school.school.model.Grade;
import com.school.school.service.GradeService;
import com.school.school.service.dto.GradeDto;
import com.school.school.service.mapper.GradeMapper;
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
@RequestMapping("/api/grades")
@AllArgsConstructor
public final class GradeController {

    private final GradeService service;

    private final GradeMapper mapper;

    @Operation(summary = "Найти оценку по индетификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Оценка найдена"),
            @ApiResponse(responseCode = "404", description = "Оценка не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<GradeDto> findById(@PathVariable final Long id) {
        Grade grade = service.findGradeById(id);

        return ResponseEntity.ok(mapper.toDto(grade));
    }

    @Operation(summary = "Найти все оценки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Оценки найдены"),
            @ApiResponse(responseCode = "404", description = "Оценки не найдены")
    })
    @GetMapping
    public ResponseEntity<List<GradeDto>> getAllGrades() {
        List<Grade> grades = service.findAllGrades();
        if (grades.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        List<GradeDto> dtoList = grades.stream()
                .map(mapper::toDto)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @Operation(summary = "Добавление оценки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Оценка добавлена"),
            @ApiResponse(responseCode = "404", description = "Оценка не найдена")
    })
    @PostMapping
    public ResponseEntity<Void> addGrade(
            @Valid @RequestBody final GradeDto gradeDto) {
        service.createGrade(gradeDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Обновить оценку по индетификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Оценка успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Оценка не найдена")
    })
    @PutMapping("/{id}")
    public ResponseEntity<GradeDto> updateGrade(
            @PathVariable final Long id,
            @Valid @RequestBody final GradeDto gradeDto) {
        Grade grade = service.updateGrade(id, gradeDto);
        return ResponseEntity.ok(mapper.toDto(grade));
    }

    @Operation(summary = "Удалить оценку по индетификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Оценка успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Оценка не найдена")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGrade(@PathVariable final Long id) {
        service.deleteGrade(id);
        return ResponseEntity.noContent().build();
    }

}
