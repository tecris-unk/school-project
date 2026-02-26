package com.school.school.controller;

import com.school.school.service.dto.response.GradeResponse;
import com.school.school.service.GradeService;
import com.school.school.service.dto.request.GradeRequest;
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

    @Operation(summary = "Найти оценку по индетификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Оценка найдена"),
            @ApiResponse(responseCode = "404", description = "Оценка не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<GradeResponse> findById(@PathVariable final Long id) {
        return ResponseEntity.ok(service.findGradeById(id));
    }

    @Operation(summary = "Найти все оценки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Оценки найдены"),
            @ApiResponse(responseCode = "204", description = "Список оценок пуст")
    })
    @GetMapping
    public ResponseEntity<List<GradeResponse>> getAllGrades() {
        List<GradeResponse> grades = service.findAllGrades();
        if (grades.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(grades);
    }

    @Operation(summary = "Добавление оценки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Оценка добавлена"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
    })
    @PostMapping
    public ResponseEntity<GradeResponse> addGrade(
            @Valid @RequestBody final GradeRequest gradeRequest) {
        GradeResponse created = service.createGrade(gradeRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Обновить оценку по индетификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Оценка успешно обновлена"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Оценка не найдена")
    })
    @PutMapping("/{id}")
    public ResponseEntity<GradeResponse> updateGrade(
            @PathVariable final Long id,
            @Valid @RequestBody final GradeRequest gradeRequest) {
        return ResponseEntity.ok(service.updateGrade(id, gradeRequest));
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
