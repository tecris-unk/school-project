package com.school.school.controller.mapper;

import com.school.school.model.Grade;
import com.school.school.model.Student;
import com.school.school.service.dto.request.StudentRequest;
import com.school.school.service.dto.response.GradeResponse;
import com.school.school.service.dto.response.StudentResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public final class StudentMapper {

    public StudentResponse toResponse(final Student student) {
        if (student == null) {
            return null;
        }

        StudentResponse response = new StudentResponse();
        response.setId(student.getId());
        response.setFirstName(student.getFirstName());
        response.setLastName(student.getLastName());
        response.setGender(student.getGender() != null ? student.getGender().name() : null);
        response.setEmail(student.getEmail());
        response.setSchoolClassId(student.getSchoolClass() != null ? student.getSchoolClass().getId() : null);
        response.setGrades(toGradeSummaries(student.getGrades()));
        return response;
    }

    public Student toEntity(final StudentRequest request) {
        if (request == null) {
            return null;
        }

        Student student = new Student();
        updateEntity(student, request);
        return student;
    }

    public void updateEntity(final Student student, final StudentRequest request) {
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setGender(parseGender(request.getGender()));
        student.setEmail(request.getEmail());
    }

    private List<GradeResponse> toGradeSummaries(final List<Grade> grades) {
        if (grades == null) {
            return new ArrayList<>();
        }

        return grades.stream().map(grade -> new GradeResponse(
                grade.getId(),
                grade.getScore(),
                grade.getDate(),
                grade.getStudent() != null ? grade.getStudent().getId() : null,
                grade.getSubject() != null ? grade.getSubject().getId() : null
        )).toList();
    }

    private Student.Gender parseGender(final String gender) {
        if (gender == null) {
            return null;
        }
        return Student.Gender.valueOf(gender.trim().toUpperCase(Locale.ROOT));
    }
}
