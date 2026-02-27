package com.school.school.service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StudentWithGradesRequest {

    @NotNull
    @Valid
    StudentRequest student;

    @Valid
    List<GradeRequest> grades;
}
