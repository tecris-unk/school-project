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

    public static final int MIN_SCORE = 2;
    public static final int MAX_SCORE = 10;

    private Long id;

    @Min(MIN_SCORE)
    @Max(MAX_SCORE)
    private Integer score;

    @NotNull
    private LocalDate date;

    private StudentDto student;

    private SubjectDto subject;
}
