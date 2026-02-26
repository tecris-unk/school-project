package com.school.school.controller.mapper;

import com.school.school.model.SchoolClass;
import com.school.school.model.Subject;
import com.school.school.service.dto.request.SubjectRequest;
import com.school.school.service.dto.response.SubjectResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public final class SubjectMapper {

    public SubjectResponse toResponse(final Subject subject) {
        if (subject == null) {
            return null;
        }

        SubjectResponse response = new SubjectResponse();
        response.setName(subject.getName());
        response.setDescription(subject.getDescription());
        response.setId(subject.getId());
        response.setTeacherId(subject.getTeacher() != null ? subject.getTeacher().getId() : null);
        response.setSchoolClassIds(toSchoolClassIds(subject.getSchoolClasses()));
        return response;
    }

    public Subject toEntity(final SubjectRequest request) {
        if (request == null) {
            return null;
        }

        Subject subject = new Subject();
        updateEntity(subject, request);
        return subject;
    }

    public void updateEntity(final Subject subject, final SubjectRequest request) {
        subject.setName(request.getName());
        subject.setDescription(request.getDescription());
    }

    private List<Long> toSchoolClassIds(final List<SchoolClass> schoolClasses) {
        if (schoolClasses == null) {
            return new ArrayList<>();
        }
        return schoolClasses.stream().map(SchoolClass::getId).toList();
    }
}
