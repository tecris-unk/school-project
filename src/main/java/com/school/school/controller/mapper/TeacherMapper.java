package com.school.school.controller.mapper;

import com.school.school.model.Teacher;
import com.school.school.service.dto.request.TeacherRequest;
import com.school.school.service.dto.response.TeacherResponse;
import org.springframework.stereotype.Component;

@Component
public final class TeacherMapper {

    public TeacherResponse toResponse(final Teacher teacher) {
        if (teacher == null) {
            return null;
        }

        TeacherResponse response = new TeacherResponse();
        response.setId(teacher.getId());
        response.setFirstName(teacher.getFirstName());
        response.setLastName(teacher.getLastName());
        response.setEmail(teacher.getEmail());
        return response;
    }

    public Teacher toEntity(final TeacherRequest request) {
        if (request == null) {
            return null;
        }

        Teacher teacher = new Teacher();
        updateEntity(teacher, request);
        return teacher;
    }

    public void updateEntity(final Teacher teacher, final TeacherRequest request) {
        teacher.setFirstName(request.getFirstName());
        teacher.setLastName(request.getLastName());
        teacher.setEmail(request.getEmail());
    }
}
