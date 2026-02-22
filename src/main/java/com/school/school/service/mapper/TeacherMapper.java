package com.school.school.service.mapper;

import com.school.school.model.Teacher;
import com.school.school.service.dto.TeacherDTO;
import org.springframework.stereotype.Component;

@Component
public final class TeacherMapper {

    public TeacherDTO toDTO(final Teacher teacher) {
        if (teacher == null) {
            return null;
        }

        return new TeacherDTO(
                teacher.getId(),
                teacher.getFirstName(),
                teacher.getLastName(),
                teacher.getEmail()
        );
    }

    public Teacher toEntity(final TeacherDTO dto) {
        if (dto == null) {
            return null;
        }

        Teacher teacher = new Teacher();
        teacher.setFirstName(dto.getFirstName());
        teacher.setLastName(dto.getLastName());
        teacher.setEmail(dto.getEmail());
        return teacher;
    }

    public void updateEntity(final Teacher teacher, final TeacherDTO dto) {
        teacher.setFirstName(dto.getFirstName());
        teacher.setLastName(dto.getLastName());
        teacher.setEmail(dto.getEmail());
    }
}
