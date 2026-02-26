package com.school.school.controller.mapper;

import com.school.school.model.SchoolClass;
import com.school.school.model.Student;
import com.school.school.model.Subject;
import com.school.school.service.dto.request.SchoolClassRequest;
import com.school.school.service.dto.response.SchoolClassResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public final class SchoolClassMapper {

    public SchoolClassResponse toResponse(final SchoolClass schoolClass) {
        if (schoolClass == null) {
            return null;
        }

        SchoolClassResponse response = new SchoolClassResponse();
        response.setId(schoolClass.getId());
        response.setGrade(schoolClass.getGrade());
        response.setLetter(schoolClass.getLetter());
        response.setStudentIds(toStudentIds(schoolClass.getStudents()));
        response.setSubjectIds(toSubjectIds(schoolClass.getSubjects()));
        return response;
    }

    public SchoolClass toEntity(final SchoolClassRequest request) {
        if (request == null) {
            return null;
        }

        SchoolClass schoolClass = new SchoolClass();
        updateEntity(schoolClass, request);
        return schoolClass;
    }

    public void updateEntity(final SchoolClass schoolClass, final SchoolClassRequest request) {
        schoolClass.setGrade(request.getGrade());
        schoolClass.setLetter(request.getLetter());
    }

    private List<Long> toStudentIds(final List<Student> students) {
        if (students == null) {
            return new ArrayList<>();
        }

        return students.stream().map(Student::getId).toList();
    }

    private List<Long> toSubjectIds(final List<Subject> subjects) {
        if (subjects == null) {
            return new ArrayList<>();
        }

        return subjects.stream().map(Subject::getId).toList();
    }
}
