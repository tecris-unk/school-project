package com.school.school.service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {
    public static final int MIN_GRADE = 1;
    public static final int MAX_GRADE = 11;
    private Long id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Min(MIN_GRADE)
    @Max(MAX_GRADE)
    private int grade;

    @NotBlank
    private String gender;

    @NotBlank
    @Email
    private String email;
}
