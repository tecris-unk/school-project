package com.school.school.service.mapper;

import com.school.school.model.Subject;
import com.school.school.model.Teacher;
import com.school.school.service.dto.SubjectDTO;
import org.springframework.stereotype.Component;

@Component
public final class SubjectMapper {

    public SubjectDTO toDTO(final Subject subject) {
        if (subject == null) {
            return null;
        }

        Long teacherId = null;
        if (subject.getTeacher() != null) {
            teacherId = subject.getTeacher().getId();
        }

        return new SubjectDTO(
                subject.getId(),
                subject.getName(),
                subject.getDescription(),
                teacherId
        );
    }

    public Subject toEntity(final SubjectDTO dto) {
        if (dto == null) {
            return null;
        }

        Subject subject = new Subject();
        subject.setName(dto.getName());
        subject.setDescription(dto.getDescription());
        if (dto.getTeacherId() != null) {
            Teacher teacher = new Teacher();
            teacher.setId(dto.getTeacherId());
            subject.setTeacher(teacher);
        }
        return subject;
    }

    public void updateEntity(final Subject subject, final SubjectDTO dto) {
        subject.setName(dto.getName());
        subject.setDescription(dto.getDescription());
        if (dto.getTeacherId() == null) {
            subject.setTeacher(null);
            return;
        }
        Teacher teacher = new Teacher();
        teacher.setId(dto.getTeacherId());
        subject.setTeacher(teacher);
    }
}