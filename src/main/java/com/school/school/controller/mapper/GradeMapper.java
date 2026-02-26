package com.school.school.controller.mapper;

import com.school.school.model.Grade;
import com.school.school.service.dto.request.GradeRequest;
import com.school.school.service.dto.response.GradeResponse;
import org.springframework.stereotype.Component;

@Component
public final class GradeMapper {

    public GradeResponse toResponse(final Grade grade) {
        if (grade == null) {
            return null;
        }

        GradeResponse response = new GradeResponse();
        response.setId(grade.getId());
        response.setScore(grade.getScore());
        response.setDate(grade.getDate());
        response.setStudentId(grade.getStudent() != null ? grade.getStudent().getId() : null);
        response.setSubjectId(grade.getSubject() != null ? grade.getSubject().getId() : null);
        return response;
    }

    public Grade toEntity(final GradeRequest request) {
        if (request == null) {
            return null;
        }

        Grade grade = new Grade();
        updateEntity(grade, request);
        return grade;
    }

    public void updateEntity(final Grade grade, final GradeRequest request) {
        grade.setScore(request.getScore());
        grade.setDate(request.getDate());
    }
}
