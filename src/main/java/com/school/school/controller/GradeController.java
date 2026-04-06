package com.school.school.controller;

import com.school.school.service.GradeService;
import com.school.school.service.dto.request.GradeRequest;
import com.school.school.service.dto.response.GradeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/grades")
@AllArgsConstructor
public class GradeController {

    private final GradeService service;


    @Operation(summary = "Найти оценку по индетификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Оценка найдена"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "404", description = "Оценка не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<GradeResponse> findById(@PathVariable @Positive final Long id) {
        return ResponseEntity.ok(service.findGradeById(id));
    }

    @Operation(summary = "Найти все оценки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Оценки найдены"),
            @ApiResponse(responseCode = "204", description = "Список оценок пуст")
    })
    @GetMapping
    public ResponseEntity<List<GradeResponse>> getAllGrades(
            @RequestParam(required = false) @Positive Long studentId,
            @RequestParam(required = false) @Positive Long subjectId,
            @RequestParam(required = false) @Min(2) Integer minScore) {
        List<GradeResponse> grades = service.findAllGrades();
        if (studentId != null) {
            grades = grades.stream()
                    .filter(grade -> studentId.equals(grade.getStudentId()))
                    .toList();
        }
        if (subjectId != null) {
            grades = grades.stream()
                    .filter(grade -> subjectId.equals(grade.getSubjectId()))
                    .toList();
        }
        if (minScore != null) {
            grades = grades.stream()
                    .filter(grade -> grade.getScore() != null && grade.getScore() >= minScore)
                    .toList();
        }

        if (grades.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(grades);
    }

    @Operation(summary = "Добавление оценки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Оценка добавлена"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Связанные сущности не найдены")
    })
    @PostMapping
    public ResponseEntity<GradeResponse> addGrade(
            @Valid @RequestBody final GradeRequest gradeRequest) {
        GradeResponse created = service.createGrade(gradeRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Массовое добавление оценок с выбором режима транзакционности")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Оценки добавлены"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Связанные сущности не найдены")
    })
    @PostMapping("/bulk")
    public ResponseEntity<List<GradeResponse>> addGradesBulk(
            @RequestParam(defaultValue = "true") final boolean transactional,
            @Valid @RequestBody final List<@Valid GradeRequest> gradeRequests) {
        List<GradeResponse> created = transactional
                ? service.createGradesBulkTransactional(gradeRequests)
                : service.createGradesBulkNonTransactional(gradeRequests);
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
            @PathVariable @Positive final Long id,
            @Valid @RequestBody final GradeRequest gradeRequest) {
        return ResponseEntity.ok(service.updateGrade(id, gradeRequest));
    }

    @Operation(summary = "Удалить оценку по индетификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Оценка успешно удалена"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "404", description = "Оценка не найдена")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGrade(@PathVariable @Positive final Long id) {
        service.deleteGrade(id);
        return ResponseEntity.noContent().build();
    }
}
