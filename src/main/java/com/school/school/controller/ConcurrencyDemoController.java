package com.school.school.controller;

import com.school.school.service.ConcurrencyDemoService;
import com.school.school.service.dto.response.RaceConditionDemoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/concurrency")
@AllArgsConstructor
public class ConcurrencyDemoController {

    private final ConcurrencyDemoService service;

    @Operation(summary = "Демонстрация race condition и потокобезопасных решений")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Демонстрация выполнена")
    })
    @GetMapping("/race-demo")
    public ResponseEntity<RaceConditionDemoResponse> runRaceDemo(
            @RequestParam(defaultValue = "60") @Min(1) final int threads,
            @RequestParam(defaultValue = "2000") @Min(1) final int incrementsPerThread
    ) {
        return ResponseEntity.ok(service.runRaceConditionDemo(threads, incrementsPerThread));
    }
}