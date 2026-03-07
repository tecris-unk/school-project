package com.school.school.service.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Запрос на создание ученика вместе с оценками")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StudentWithGradesRequest {

    @NotNull
    @Valid
    @Schema(description = "Данные ученика")
    private StudentRequest student;

    @Valid
    @ArraySchema(schema = @Schema(implementation = GradeRequest.class),
            arraySchema = @Schema(description = "Список оценок ученика"))
    private List<GradeRequest> grades;
}
