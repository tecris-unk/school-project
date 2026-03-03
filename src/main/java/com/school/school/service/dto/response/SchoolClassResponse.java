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
public class SchoolClassResponse {

    private Long id;
    private Integer grade;
    private String letter;
    private List<Long> studentIds = new ArrayList<>();
    private List<Long> subjectIds = new ArrayList<>();
}