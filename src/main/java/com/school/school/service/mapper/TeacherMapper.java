package com.school.school.service.mapper;

import com.school.school.model.Teacher;
import com.school.school.service.dto.TeacherDto;
import org.springframework.stereotype.Component;

@Component
public final class TeacherMapper {

    public TeacherDto toDto(final Teacher teacher) {
        if (teacher == null) {
            return null;
        }

        return new TeacherDto(
                teacher.getId(),
                teacher.getFirstName(),
                teacher.getLastName(),
                teacher.getEmail()
        );
    }

    public Teacher toEntity(final TeacherDto dto) {
        if (dto == null) {
            return null;
        }

        Teacher teacher = new Teacher();
        teacher.setFirstName(dto.getFirstName());
        teacher.setLastName(dto.getLastName());
        teacher.setEmail(dto.getEmail());
        return teacher;
    }

    public void updateEntity(final Teacher teacher, final TeacherDto dto) {
        teacher.setFirstName(dto.getFirstName());
        teacher.setLastName(dto.getLastName());
        teacher.setEmail(dto.getEmail());
    }
}
