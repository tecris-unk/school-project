package com.school.school.service.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StudentDTO {
    /**
     * Минимальная оценка.
     */
    public static final int MIN_GRADE = 1;
    /**
     * Максимальная оценка.
     */
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
