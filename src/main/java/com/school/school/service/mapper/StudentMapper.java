package com.school.school.service.mapper;

import com.school.school.model.Student;
import com.school.school.service.dto.StudentDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

@Component
public final class StudentMapper {

    @NotNull
    public StudentDTO toDTO(final Student student) {
        return new StudentDTO(student);
    }

    public Student toEntity(final StudentDTO dto) {
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
    }
}
