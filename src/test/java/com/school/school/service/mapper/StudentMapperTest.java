package com.school.school.service.mapper;

import com.school.school.model.Student;
import com.school.school.service.dto.StudentDto;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class StudentMapperTest {

    private final StudentMapper mapper = new StudentMapper();

    @Test
    void toDtoShouldReturnNullWhenStudentIsNull() {
        StudentDto result = mapper.toDto(null);

        assertNull(result);
    }

    @Test
    void toDtoShouldMapAllFields() {
        Student student = new Student(1L, "Ivan", "Petrov", 10, Student.Gender.MALE, "ivan@mail.com", null, null);

        StudentDto result = mapper.toDto(student);

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
        StudentDto dto = new StudentDto(7L, "Anna", "Smirnova", 9, "FEMALE", "anna@mail.com");

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
        Student existing = new Student(3L, "Old", "Name", 5, Student.Gender.MALE, "old@mail.com", null, null);
        StudentDto dto = new StudentDto(null, "New", "Student", 11, "FEMALE", "new@mail.com");

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

    @Test
    void toEntityShouldSupportCaseInsensitiveGender() {
        StudentDto dto = new StudentDto(null, "Anna", "Smirnova", 9, "female", "anna@mail.com");

        Student result = mapper.toEntity(dto);

        assertEquals(Student.Gender.FEMALE, result.getGender());
    }
}
