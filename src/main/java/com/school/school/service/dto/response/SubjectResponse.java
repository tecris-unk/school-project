package com.school.school.service.dto.response;

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
public class SubjectResponse {
    private Long id;
    private Long teacherId;
    private List<Long> schoolClassIds = new ArrayList<>();
}