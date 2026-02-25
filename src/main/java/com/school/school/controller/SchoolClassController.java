package com.school.school.controller;

import com.school.school.model.SchoolClass;
import com.school.school.service.SchoolClassService;
import com.school.school.service.dto.SchoolClassDto;
import com.school.school.service.mapper.SchoolClassMapper;
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

    private final SchoolClassMapper mapper;

    @Operation(summary = "Найти класс по индетификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Класс найден"),
            @ApiResponse(responseCode = "404", description = "Класс не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SchoolClassDto> findById(
            @PathVariable final Long id) {
        SchoolClass schoolClass = service.findClassById(id);
        if (schoolClass == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDto(schoolClass));
    }

    @Operation(summary = "Получить все классы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Классы получены"),
            @ApiResponse(responseCode = "404", description = "Классы не найдены")
    })
    @GetMapping
    public ResponseEntity<List<SchoolClassDto>> getAllClasses() {
        List<SchoolClass> classes = service.findAllClasses();
        if (classes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        List<SchoolClassDto> dtoList = classes.stream()
                .map(mapper::toDto)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @Operation(summary = "Получить все классы c загруженными предметами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Классы получены"),
            @ApiResponse(responseCode = "404", description = "Классы не найдены")
    })
    @GetMapping("/with-subjects")
    public ResponseEntity<List<SchoolClassDto>> getAllSchoolClassesWithSubjects() {
        List<SchoolClass> schoolClasses = service.findAllSchoolClassesWithSubjects();
        if (schoolClasses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        List<SchoolClassDto> dtoList = schoolClasses.stream()
                .map(mapper::toDto)
                .toList();
        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }

    @Operation(summary = "Создать новый класс")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Класс создан"),
    })
    @PostMapping
    public ResponseEntity<Void> addClass(
            @Valid @RequestBody final SchoolClassDto classDto) {
        service.createClass(classDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
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
        SchoolClass schoolClass = service.updateClass(id, classDto);
        if (schoolClass == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(mapper.toDto(schoolClass));
    }

    @Operation(summary = "Удалить класс")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Класс успешно удален"),
            @ApiResponse(responseCode = "404", description = "Класс не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable final Long id) {
        boolean isDeleted = service.deleteClass(id);
        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.noContent().build();
    }
}
