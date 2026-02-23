package com.school.school.service.mapper;

import com.school.school.model.Grade;
import com.school.school.service.dto.GradeDto;
import org.springframework.stereotype.Component;

@Component
public final class GradeMapper {

    public GradeDto toDto(final Grade grade) {
        if (grade == null) {
            return null;
        }

        Long studentId = grade.getStudent() != null ? grade.getStudent().getId() : null;
        Long subjectId = grade.getSubject() != null ? grade.getSubject().getId() : null;

        return new GradeDto(
                grade.getId(),
                studentId,
                subjectId,
                grade.getScore(),
                grade.getDate()
        );
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
}
