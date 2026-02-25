package com.school.school.controller;

import com.school.school.service.SchoolClassService;
import com.school.school.service.dto.SchoolClassDto;
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
@RequestMapping("/api/classes")
@AllArgsConstructor
public final class SchoolClassController {

    private final SchoolClassService service;

    @Operation(summary = "Найти класс по индетификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Класс найден"),
            @ApiResponse(responseCode = "404", description = "Класс не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SchoolClassDto> findById(
            @PathVariable final Long id) {
        return ResponseEntity.ok(service.findClassById(id));
    }

    @Operation(summary = "Получить все классы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Классы получены"),
            @ApiResponse(responseCode = "404", description = "Классы не найдены")
    })
    @GetMapping
    public ResponseEntity<List<SchoolClassDto>> getAllClasses() {
        List<SchoolClassDto> classes = service.findAllClasses();
        if (classes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return ResponseEntity.ok(classes);
    }

    @Operation(summary = "Получить все классы c загруженными предметами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Классы получены"),
            @ApiResponse(responseCode = "404", description = "Классы не найдены")
    })
    @GetMapping("/with-subjects")
    public ResponseEntity<List<SchoolClassDto>> getAllSchoolClassesWithSubjects() {
        List<SchoolClassDto> schoolClasses = service.findAllSchoolClassesWithSubjects();
        if (schoolClasses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return new ResponseEntity<>(schoolClasses, HttpStatus.OK);
    }

    @Operation(summary = "Создать новый класс")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Класс создан"),
    })
    @PostMapping
    public ResponseEntity<SchoolClassDto> addClass(
            @Valid @RequestBody final SchoolClassDto classDto) {
        SchoolClassDto created = service.createClass(classDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Обновить класс")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Класс успешно обновлен"),
            @ApiResponse(responseCode = "404", description = "Класс не найден")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SchoolClassDto> updateClass(
            @PathVariable final Long id,
            @Valid @RequestBody final SchoolClassDto classDto) {
        return ResponseEntity.ok(service.updateClass(id, classDto));
    }

    @Operation(summary = "Удалить класс")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Класс успешно удален"),
            @ApiResponse(responseCode = "404", description = "Класс не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable final Long id) {
        service.deleteClass(id);
        return ResponseEntity.noContent().build();
    }
}
