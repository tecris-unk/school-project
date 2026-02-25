package com.school.school.service.mapper;

import com.school.school.model.Grade;
import com.school.school.model.Student;
import com.school.school.model.Subject;
import com.school.school.service.dto.GradeDto;
import com.school.school.service.dto.StudentDto;
import com.school.school.service.dto.SubjectDto;
import org.springframework.stereotype.Component;

@Component
public final class GradeMapper {

    public GradeDto toDto(final Grade grade) {
        if (grade == null) {
            return null;
        }

        GradeDto dto = new GradeDto();
        dto.setId(grade.getId());
        dto.setStudent(toStudentSummary(grade.getStudent()));
        dto.setSubject(toSubjectSummary(grade.getSubject()));
        dto.setScore(grade.getScore());
        dto.setDate(grade.getDate());
        return dto;
    }

    public Grade toEntity(final GradeDto dto) {
        if (dto == null) {
            return null;
        }

        Grade grade = new Grade();
        grade.setScore(dto.getScore());
        grade.setDate(dto.getDate());
        return grade;
    }

    public void updateEntity(final Grade grade, final GradeDto dto) {
        grade.setScore(dto.getScore());
        grade.setDate(dto.getDate());
    }

    private StudentDto toStudentSummary(final Student student) {
        if (student == null) {
            return null;
        }

        StudentDto dto = new StudentDto();
        dto.setId(student.getId());
        dto.setFirstName(student.getFirstName());
        dto.setLastName(student.getLastName());
        dto.setGender(student.getGender() != null ? student.getGender().name() : null);
        dto.setEmail(student.getEmail());
        return dto;
    }

    private SubjectDto toSubjectSummary(final Subject subject) {
        if (subject == null) {
            return null;
        }

        SubjectDto dto = new SubjectDto();
        dto.setId(subject.getId());
        dto.setName(subject.getName());
        dto.setDescription(subject.getDescription());
        return dto;
    }
}
