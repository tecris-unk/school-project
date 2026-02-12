package com.school.school.service.mapper;

import com.school.school.model.Student;
import com.school.school.service.dto.StudentDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

@Component
public final class StudentMapper {

    @NotNull
    public StudentDTO toDTO(final Student student) {
        if (student == null) {
           return null;
        }

        return new StudentDTO(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getGrade(),
                student.getGender().name(),
                student.getEmail()
        );
    }

    public Student toEntity(final StudentDTO dto) {
        if (dto == null) {
            return null;
        }

        Student student = new Student();
        // ID НЕ ТРОГАЕМ ОНО ГЕНЕРИТСЯ САМО
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setGrade(dto.getGrade());
        student.setGender(Student.Gender.valueOf(dto.getGender()));
        student.setEmail(dto.getEmail());

        return student;
    }

    public void updateEntity(final Student student, final StudentDTO dto) {
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setGrade(dto.getGrade());
        student.setGender(Student.Gender.valueOf(dto.getGender()));
        student.setEmail(dto.getEmail());
    }
}
