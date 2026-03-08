package com.school.school.service.dto.response;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GradeResponse {

    private Long id;
    private Integer score;
    private LocalDate date;
    private Long studentId;
    private Long subjectId;
}
