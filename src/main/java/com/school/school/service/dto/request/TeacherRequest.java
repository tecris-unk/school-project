package com.school.school.service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Запрос на создание или обновление учителя")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TeacherRequest {

    @NotBlank
    @Schema(description = "Имя учителя", example = "Мария")
    private String firstName;

    @NotBlank
    @Schema(description = "Фамилия учителя", example = "Петрова")
    private String lastName;

    @NotBlank
    @Email
    @Schema(description = "Email учителя", example = "maria.petrova@example.com")
    private String email;
}
