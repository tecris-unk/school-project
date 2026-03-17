package com.school.school.controller;

import com.school.school.service.async.AsyncBusinessTaskService;
import com.school.school.service.async.TaskStatus;
import com.school.school.service.dto.response.TaskCreatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/tasks")
@AllArgsConstructor
public class AsyncTaskController {

    private final AsyncBusinessTaskService service;

    @Operation(summary = "Запустить асинхронную бизнес-операцию")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Задача запущена")
    })
    @PostMapping
    public ResponseEntity<TaskCreatedResponse> startTask() {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new TaskCreatedResponse(service.startTask()));
    }

    @Operation(summary = "Проверить статус асинхронной задачи")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус получен"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskStatus> getTaskStatus(@PathVariable final String taskId) {
        return ResponseEntity.ok(service.getTaskStatus(taskId));
    }
}