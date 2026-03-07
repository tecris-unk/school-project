package com.school.school.service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Запрос на создание или обновление класса")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class SchoolClassRequest {
    public static final int MIN_GRADE = 1;
    public static final int MAX_GRADE = 11;

    @Min(MIN_GRADE)
    @Max(MAX_GRADE)
    @NotNull
    @Schema(description = "Параллель класса", example = "10", minimum = "1", maximum = "11")
    private Integer grade;

    @NotBlank
    @Schema(description = "Буква класса", example = "A")
    private String letter;
}
