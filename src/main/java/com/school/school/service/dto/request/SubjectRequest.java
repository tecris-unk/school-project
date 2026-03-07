package com.school.school.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Запрос на создание или обновление предмета")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SubjectRequest {

    @NotBlank
    @Schema(description = "Название предмета", example = "Mathematics")
    private String name;

    @Schema(description = "Описание предмета", example = "Алгебра и геометрия")
    private String description;

    @Schema(description = "ID учителя", example = "5")
    private Long teacherId;
}
