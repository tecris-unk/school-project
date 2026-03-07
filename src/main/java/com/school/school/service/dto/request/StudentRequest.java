package com.school.school.service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Запрос на создание или обновление ученика")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class StudentRequest {

    @NotBlank
    @Schema(description = "Имя ученика", example = "Иван")
    private String firstName;

    @NotBlank
    @Schema(description = "Фамилия ученика", example = "Иванов")
    private String lastName;

    @NotBlank
    @Schema(description = "Пол ученика", example = "male")
    private String gender;

    @NotBlank
    @Email
    @Schema(description = "Email ученика", example = "ivan.ivanov@example.com")
    private String email;

    @Schema(description = "ID класса", example = "3")
    private Long schoolClassId;
}
