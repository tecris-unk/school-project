package com.school.school.service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GradeDto {
    /**
     * Минимальная оценка.
     */
    public static final int MIN_SCORE = 2;
    /**
     * Максимальная оценка.
     */
    public static final int MAX_SCORE = 10;

    private Long id;

    @NotNull
    private Long studentId;

    @NotNull
    private Long subjectId;

    @Min(MIN_SCORE)
    @Max(MAX_SCORE)
    private Integer score;

    @NotNull
    private LocalDate date;
}
