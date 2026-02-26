package com.school.school.service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class SchoolClassRequest {
    public static final int MIN_GRADE = 1;
    public static final int MAX_GRADE = 11;

    @Min(MIN_GRADE)
    @Max(MAX_GRADE)
    @NotBlank
    private Integer grade;

    @NotBlank
    private String letter;
}
