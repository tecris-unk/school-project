package com.school.school.service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
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
    private StudentRequest student;

    @Valid
    private List<GradeRequest> grades = new ArrayList<>();
}