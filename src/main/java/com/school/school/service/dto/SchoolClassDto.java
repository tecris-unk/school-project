package com.school.school.service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SchoolClassDto {
    /**
     * Минимальная оценка.
     */
    public static final int MIN_GRADE = 1;
    /**
     * Максимальная оценка.
     */
    public static final int MAX_GRADE = 11;

    private Long id;

    @Min(MIN_GRADE)
    @Max(MAX_GRADE)
    private Integer grade;

    @NotBlank
    private String letter;

    private List<StudentDto> students = new ArrayList<>();

    private List<SubjectDto> subjects = new ArrayList<>();
}
