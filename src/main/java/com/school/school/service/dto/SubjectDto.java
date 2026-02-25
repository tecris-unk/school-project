package com.school.school.service.dto;

import jakarta.validation.constraints.NotBlank;
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
public class SubjectDto {
    private Long id;

    @NotBlank
    private String name;

    private String description;

    private TeacherDto teacher;

    private List<GradeDto> grades = new ArrayList<>();

    private List<SchoolClassDto> schoolClasses = new ArrayList<>();
}
