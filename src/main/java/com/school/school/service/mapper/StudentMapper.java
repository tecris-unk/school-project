package com.school.school.service.mapper;

import com.school.school.model.Student;
import com.school.school.service.dto.StudentDto;
import jakarta.validation.constraints.NotNull;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public final class StudentMapper {

    @NotNull
    public StudentDto toDto(final Student student) {
        if (student == null) {
            return null;
        }

        return new StudentDto(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getGender() != null ? student.getGender().name() : null,
                student.getEmail()
        );
    }

    public Student toEntity(final StudentDto dto) {
        if (dto == null) {
            return null;
        }

        Student student = new Student();
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setGender(parseGender(dto.getGender()));
        student.setEmail(dto.getEmail());

        return student;
    }

    public void updateEntity(final Student student, final StudentDto dto) {
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setGender(parseGender(dto.getGender()));
        student.setEmail(dto.getEmail());
    }

    private Student.Gender parseGender(final String gender) {
        if (gender == null) {
            return null;
        }
        return Student.Gender.valueOf(gender.trim().toUpperCase(Locale.ROOT));
    }
}
