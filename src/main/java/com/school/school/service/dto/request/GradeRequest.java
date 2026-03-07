package com.school.school.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Запрос на создание или обновление оценки")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class GradeRequest {

    public static final int MIN_SCORE = 2;
    public static final int MAX_SCORE = 10;

    @NotNull
    @Min(MIN_SCORE)
    @Max(MAX_SCORE)
    @Schema(description = "Балл", example = "8", minimum = "2", maximum = "10")
    private Integer score;

    @NotNull
    @Schema(description = "Дата выставления оценки", example = "2025-01-20")
    private LocalDate date;

    @Schema(description = "ID ученика", example = "1")
    private Long studentId;

    @NotNull
    @Schema(description = "ID предмета", example = "2")
    private Long subjectId;
}
