package com.school.school.controller;

import com.school.school.service.SchoolClassService;
import com.school.school.service.dto.request.SchoolClassRequest;
import com.school.school.service.dto.response.SchoolClassResponse;
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
@RequestMapping("/api/classes")
@AllArgsConstructor
public class SchoolClassController {

    private final SchoolClassService service;

    @Operation(summary = "Найти класс по индетификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Класс найден"),
            @ApiResponse(responseCode = "404", description = "Класс не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SchoolClassResponse> findById(
            @PathVariable @Positive final Long id) {
        return ResponseEntity.ok(service.findClassById(id));
    }

    @Operation(summary = "Получить все классы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Классы получены"),
            @ApiResponse(responseCode = "204", description = "Список классов пуст")
    })
    @GetMapping
    public ResponseEntity<List<SchoolClassResponse>> getAllClasses() {
        List<SchoolClassResponse> classes = service.findAllClasses();
        if (classes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(classes);
    }

    @Operation(summary = "Получить все классы c загруженными предметами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Классы получены"),
            @ApiResponse(responseCode = "204", description = "Список классов пуст")
    })
    @GetMapping("/with-subjects")
    public ResponseEntity<List<SchoolClassResponse>> getAllSchoolClassesWithSubjects() {
        List<SchoolClassResponse> schoolClasses = service.findAllSchoolClassesWithSubjects();
        if (schoolClasses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return new ResponseEntity<>(schoolClasses, HttpStatus.OK);
    }

    @Operation(summary = "Создать новый класс")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Класс создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
    })
    @PostMapping
    public ResponseEntity<SchoolClassResponse> addClass(
            @Valid @RequestBody final SchoolClassRequest classRequest) {
        SchoolClassResponse created = service.createClass(classRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Обновить класс")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Класс успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Класс не найден")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SchoolClassResponse> updateClass(
            @PathVariable @Positive final Long id,
            @Valid @RequestBody final SchoolClassRequest classRequest) {
        return ResponseEntity.ok(service.updateClass(id, classRequest));
    }

    @Operation(summary = "Добавить предмет в класс")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предмет добавлен в класс"),
            @ApiResponse(responseCode = "404", description = "Класс или предмет не найден")
    })
    @PutMapping("/{classId}/subjects/{subjectId}")
    public ResponseEntity<SchoolClassResponse> addSubjectToClass(
            @PathVariable @Positive final Long classId,
            @PathVariable @Positive final Long subjectId) {
        return ResponseEntity.ok(service.addSubjectToClass(classId, subjectId));
    }

    @Operation(summary = "Удалить класс")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Класс успешно удален"),
            @ApiResponse(responseCode = "404", description = "Класс не найден")
    })

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable @Positive final Long id) {
        service.deleteClass(id);
        return ResponseEntity.noContent().build();
    }
}
