package com.school.school.service;

import com.school.school.model.Grade;
import com.school.school.model.Student;
import com.school.school.model.Subject;
import com.school.school.repository.GradeRepository;
import com.school.school.repository.StudentRepository;
import com.school.school.repository.SubjectRepository;
import com.school.school.service.dto.StudentDTO;
import com.school.school.service.mapper.StudentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository repository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private StudentServiceImpl service;

    @Mock
    private StudentMapper mapper;

    @BeforeEach
    void setUp() {
        service = new StudentServiceImpl(repository, subjectRepository, gradeRepository, new StudentMapper());
    }

    @Test
    void findStudentByIdShouldReturnNullWhenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        Student result = service.findStudentById(1L);

        assertNull(result);
    }

    @Test
    void deleteStudentShouldReturnTrueAndDeleteWhenFound() {
        Student student = new Student();
        when(repository.findById(1L)).thenReturn(Optional.of(student));

        boolean result = service.deleteStudent(1L);

        assertTrue(result);
        verify(repository).delete(student);
    }

    @Test
    void updateStudentShouldUseExistingEntityIfFound() {
        Student existing = new Student();
        StudentDTO dto = new StudentDTO(null, "New", "Name", 11, "FEMALE", "new@mail.com");
        when(repository.findById(2L)).thenReturn(Optional.of(existing));

        Student result = service.updateStudent(2L, dto);

        assertSame(existing, result);
        assertAll(
                () -> assertEquals("New", result.getFirstName()),
                () -> assertEquals("Name", result.getLastName()),
                () -> assertEquals(11, result.getGrade()),
                () -> assertEquals(Student.Gender.FEMALE, result.getGender()),
                () -> assertEquals("new@mail.com", result.getEmail())
        );
        verify(repository).save(existing);
    }

    @Test
    void createStudentWithGradesShouldThrowIfSubjectMissing() {
        Student student = new Student();
        StudentDTO dto = mapper.toDTO(student);
        Subject gradeSubjectRef = new Subject();
        gradeSubjectRef.setId(404L);

        Grade grade = new Grade();
        grade.setSubject(gradeSubjectRef);

        when(subjectRepository.findById(404L)).thenReturn(Optional.empty());
        List<Grade> grades = List.of(grade);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createStudentWithGrades(dto, grades));

        assertEquals("Subject not found", ex.getMessage());
        verify(gradeRepository, never()).save(any());
    }
}