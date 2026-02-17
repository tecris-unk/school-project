package com.school.school.service.mapper;

import com.school.school.model.Student;
import com.school.school.service.dto.StudentDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudentMapperTest {

    private final StudentMapper mapper = new StudentMapper();

    @Test
    void toDTOShouldReturnNullWhenStudentIsNull() {
        StudentDTO result = mapper.toDTO(null);

        assertNull(result);
    }

    @Test
    void toDTOShouldMapAllFields() {
        Student student = new Student(1L, "Ivan", "Petrov", 10, Student.Gender.MALE, "ivan@mail.com", null);

        StudentDTO result = mapper.toDTO(student);

        assertAll(
                () -> assertEquals(1L, result.getId()),
                () -> assertEquals("Ivan", result.getFirstName()),
                () -> assertEquals("Petrov", result.getLastName()),
                () -> assertEquals(10, result.getGrade()),
                () -> assertEquals("MALE", result.getGender()),
                () -> assertEquals("ivan@mail.com", result.getEmail())
        );
    }

    @Test
    void toEntityShouldReturnNullWhenDtoIsNull() {
        Student result = mapper.toEntity(null);

        assertNull(result);
    }

    @Test
    void toEntityShouldMapAllFieldsExceptId() {
        StudentDTO dto = new StudentDTO(7L, "Anna", "Smirnova", 9, "FEMALE", "anna@mail.com");

        Student result = mapper.toEntity(dto);

        assertAll(
                () -> assertNull(result.getId()),
                () -> assertEquals("Anna", result.getFirstName()),
                () -> assertEquals("Smirnova", result.getLastName()),
                () -> assertEquals(9, result.getGrade()),
                () -> assertEquals(Student.Gender.FEMALE, result.getGender()),
                () -> assertEquals("anna@mail.com", result.getEmail())
        );
    }

    @Test
    void updateEntityShouldOverwriteExistingFields() {
        Student existing = new Student(3L, "Old", "Name", 5, Student.Gender.MALE, "old@mail.com", null);
        StudentDTO dto = new StudentDTO(null, "New", "Student", 11, "FEMALE", "new@mail.com");

        mapper.updateEntity(existing, dto);

        assertAll(
                () -> assertEquals(3L, existing.getId()),
                () -> assertEquals("New", existing.getFirstName()),
                () -> assertEquals("Student", existing.getLastName()),
                () -> assertEquals(11, existing.getGrade()),
                () -> assertEquals(Student.Gender.FEMALE, existing.getGender()),
                () -> assertEquals("new@mail.com", existing.getEmail())
        );
    }
}