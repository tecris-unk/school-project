package com.school.school.service.mapper;

import com.school.school.model.Subject;
import com.school.school.service.dto.SubjectDto;
import org.springframework.stereotype.Component;

@Component
public final class SubjectMapper {

    public SubjectDto toDto(final Subject subject) {
        if (subject == null) {
            return null;
        }

        Long teacherId = null;
        if (subject.getTeacher() != null) {
            teacherId = subject.getTeacher().getId();
        }

        return new SubjectDto(
                subject.getId(),
                subject.getName(),
                subject.getDescription(),
                teacherId
        );
    }

    public Subject toEntity(final SubjectDto dto) {
        if (dto == null) {
            return null;
        }

        Subject subject = new Subject();
        subject.setName(dto.getName());
        subject.setDescription(dto.getDescription());
        return subject;
    }

    public void updateEntity(final Subject subject, final SubjectDto dto) {
        subject.setName(dto.getName());
        subject.setDescription(dto.getDescription());
    }
}
