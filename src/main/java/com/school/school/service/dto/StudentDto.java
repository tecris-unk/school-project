package com.school.school.service.dto;

import jakarta.validation.constraints.Email;
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
public class StudentDto {

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
