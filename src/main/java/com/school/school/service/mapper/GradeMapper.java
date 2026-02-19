package com.school.school.service.mapper;

import com.school.school.model.Grade;
import com.school.school.model.Student;
import com.school.school.model.Subject;
import com.school.school.service.dto.GradeDTO;
import org.springframework.stereotype.Component;

@Component
public final class GradeMapper {

    public GradeDTO toDTO(final Grade grade) {
        if (grade == null) {
            return null;
        }

        Long studentId = grade.getStudent() != null ? grade.getStudent().getId() : null;
        Long subjectId = grade.getSubject() != null ? grade.getSubject().getId() : null;

        return new GradeDTO(
                grade.getId(),
                studentId,
                subjectId,
                grade.getScore(),
                grade.getDate()
        );
    }

    public Grade toEntity(final GradeDTO dto) {
        if (dto == null) {
            return null;
        }

        Grade grade = new Grade();

        Student student = new Student();
        student.setId(dto.getStudentId());
        grade.setStudent(student);

        Subject subject = new Subject();
        subject.setId(dto.getSubjectId());
        grade.setSubject(subject);

        grade.setScore(dto.getScore());
        grade.setDate(dto.getDate());
        return grade;
    }

    public void updateEntity(final Grade grade, final GradeDTO dto) {
        Student student = new Student();
        student.setId(dto.getStudentId());
        grade.setStudent(student);

        Subject subject = new Subject();
        subject.setId(dto.getSubjectId());
        grade.setSubject(subject);

        grade.setScore(dto.getScore());
        grade.setDate(dto.getDate());
    }
}