package com.school.school.service.mapper;

import com.school.school.model.Student;
import com.school.school.service.dto.StudentDTO;
import jakarta.validation.constraints.NotNull;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public final class StudentMapper {

    /**
     * Функция, преобразующая Entity в DTO.
     *
     * @param student entity
     * @return DTO
     */
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
                student.getGender() != null ? student.getGender().name() : null,
                student.getEmail()
        );
    }

    /**
     * Функция, преобразующая DTO в Entity.
     *
     * @param dto DTO
     * @return Entity
     */
    public Student toEntity(final StudentDTO dto) {
        if (dto == null) {
            return null;
        }

        Student student = new Student();
        // ID НЕ ТРОГАЕМ ОНО ГЕНЕРИТСЯ САМО
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setGrade(dto.getGrade());
        student.setGender(parseGender(dto.getGender()));
        student.setEmail(dto.getEmail());

        return student;
    }

    /**
     * функция, изменяющая Entity из DTO
     *
     * @param student Entity, который будет изменен
     * @param dto DTO, обновление
     */
    public void updateEntity(final Student student, final StudentDTO dto) {
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setGrade(dto.getGrade());
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
