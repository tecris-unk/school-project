package com.school.school.controller;

import com.school.school.model.Subject;
import com.school.school.service.SubjectService;
import com.school.school.service.dto.SubjectDto;
import com.school.school.service.mapper.SubjectMapper;
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
@RequestMapping("/api/subjects")
@AllArgsConstructor
public final class SubjectController {

    private final SubjectService service;

    private final SubjectMapper mapper;

    @Operation(summary = "Получить предмет по id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предмет найден"),
            @ApiResponse(responseCode = "404", description = "Предмет не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SubjectDto> findById(@PathVariable final Long id) {
        Subject subject = service.findSubjectById(id);
        return ResponseEntity.ok(mapper.toDto(subject));
    }

    @Operation(summary = "Получить все предметы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предметы найдены"),
            @ApiResponse(responseCode = "204", description = "Предметы не найдены")
    })
    @GetMapping
    public ResponseEntity<List<SubjectDto>> getAllSubjects() {
        List<Subject> subjects = service.findAllSubjects();
        if (subjects.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        List<SubjectDto> dtoList = subjects.stream()
                .map(mapper::toDto)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @Operation(summary = "Создать предмет")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Предмет создан"),
            @ApiResponse(responseCode = "404", description = "Связанные сущности не найдены")
    })
    @PostMapping
    public ResponseEntity<Void> addSubject(
            @Valid @RequestBody final SubjectDto subjectDto) {
        service.createSubject(subjectDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Обновить предмет")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предмет обновлён"),
            @ApiResponse(responseCode = "404", description = "Предмет не найден")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SubjectDto> updateSubject(
            @PathVariable final Long id,
            @Valid @RequestBody final SubjectDto subjectDto) {
        Subject subject = service.updateSubject(id, subjectDto);
        return ResponseEntity.ok(mapper.toDto(subject));
    }

    @Operation(summary = "Удалить предмет")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Предмет удалён"),
            @ApiResponse(responseCode = "404", description = "Предмет не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable final Long id) {
         service.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }
}
