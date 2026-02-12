package com.school.school.service.dto;

import com.school.school.model.Student;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {

    private Long id;

    private String firstName;

    private String lastName;

    private int grade;

    private String gender;

    private String email;

    @NotNull
    public StudentDTO(Student student) {
        this.id = student.getId();
        this.firstName = student.getFirstName();
        this.lastName = student.getLastName();
        this.grade = student.getGrade();
        this.gender = student.getGender().name();
        this.email = student.getEmail();
    }
}
