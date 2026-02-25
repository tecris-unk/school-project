package com.school.school.service.mapper;

import com.school.school.model.Subject;
import com.school.school.model.Teacher;
import com.school.school.service.dto.SubjectDto;
import com.school.school.service.dto.TeacherDto;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public final class TeacherMapper {

    public TeacherDto toDto(final Teacher teacher) {
        if (teacher == null) {
            return null;
        }

        TeacherDto dto = new TeacherDto();
        dto.setId(teacher.getId());
        dto.setFirstName(teacher.getFirstName());
        dto.setLastName(teacher.getLastName());
        dto.setEmail(teacher.getEmail());
        dto.setSubjects(toSubjectSummaries(teacher.getSubjects()));
        return dto;
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

    private List<SubjectDto> toSubjectSummaries(final List<Subject> subjects) {
        if (subjects == null) {
            return new ArrayList<>();
        }

        return subjects.stream().map(subject -> {
            SubjectDto dto = new SubjectDto();
            dto.setId(subject.getId());
            dto.setName(subject.getName());
            dto.setDescription(subject.getDescription());
            return dto;
        }).toList();
    }
}
