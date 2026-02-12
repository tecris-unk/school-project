package com.school.school.service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {

    private Long id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Min(1)
    @Max(11)
    private int grade;

    @NotBlank
    private String gender;

    @NotBlank
    @Email
    private String email;
}
