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
public class TeacherResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private List<SubjectResponse> subjects = new ArrayList<>();
}