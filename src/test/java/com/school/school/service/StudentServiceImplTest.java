package com.school.school.service;

import com.school.school.model.Grade;
import com.school.school.model.Student;
import com.school.school.model.Subject;
import com.school.school.repository.GradeRepository;
import com.school.school.repository.StudentRepository;
import com.school.school.repository.SubjectRepository;
import com.school.school.service.dto.StudentDto;
import com.school.school.service.mapper.StudentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private StudentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new StudentServiceImpl(repository, subjectRepository, gradeRepository, new StudentMapper());
    }

    @Test
    void findAllStudentsShouldReturnRepositoryResult() {
        List<Student> students = List.of(new Student(), new Student());
        when(repository.findAll()).thenReturn(students);

        List<Student> result = service.findAllStudents();

        assertEquals(students, result);
        verify(repository).findAll();
    }

    @Test
    void createStudentShouldMapAndSaveEntity() {
        StudentDto dto = new StudentDto(null, "Ivan", "Petrov", 10, "MALE", "ivan@mail.com");

        service.createStudent(dto);

        verify(repository).save(argThat(student ->
                "Ivan".equals(student.getFirstName())
                        && "Petrov".equals(student.getLastName())
                        && student.getGrade() == 10
                        && Student.Gender.MALE == student.getGender()
                        && "ivan@mail.com".equals(student.getEmail())
        ));
    }

    @Test
    void findStudentByIdShouldReturnStudentWhenFound() {
        Student student = new Student();
        when(repository.findById(1L)).thenReturn(Optional.of(student));

        Student result = service.findStudentById(1L);

        assertSame(student, result);
    }

    @Test
    void findStudentByIdShouldReturnNullWhenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        Student result = service.findStudentById(1L);

        assertNull(result);
    }

    @Test
    void findStudentByEmailShouldReturnStudentWhenFound() {
        Student student = new Student();
        when(repository.findByEmail("user@mail.com")).thenReturn(Optional.of(student));

        Student result = service.findStudentByEmail("user@mail.com");

        assertSame(student, result);
    }

    @Test
    void findStudentByEmailShouldReturnNullWhenNotFound() {
        when(repository.findByEmail("unknown@mail.com")).thenReturn(Optional.empty());

        Student result = service.findStudentByEmail("unknown@mail.com");

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
    void deleteStudentShouldReturnFalseWhenMissing() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        boolean result = service.deleteStudent(1L);

        assertFalse(result);
        verify(repository, never()).delete(any());
    }

    @Test
    void updateStudentShouldUseExistingEntityIfFound() {
        Student existing = new Student();
        StudentDto dto = new StudentDto(null, "New", "Name", 11, "FEMALE", "new@mail.com");
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
    void updateStudentShouldReturnNullWhenMissing() {
        StudentDto dto = new StudentDto(null, "New", "Student", 8, "MALE", "new@student.com");
        when(repository.findById(2L)).thenReturn(Optional.empty());

        Student result = service.updateStudent(2L, dto);

        assertNull(result);
        verify(repository, never()).save(any());
    }

    @Test
    void createStudentWithGradesShouldSaveStudentAndGrades() {
        StudentDto dto = new StudentDto(null, "Ivan", "Petrov", 10, "MALE", "ivan@mail.com");
        Subject subjectReference = new Subject();
        subjectReference.setId(10L);

        Subject resolvedSubject = new Subject();
        resolvedSubject.setId(10L);

        Grade grade = new Grade();
        grade.setSubject(subjectReference);

        when(subjectRepository.findById(10L)).thenReturn(Optional.of(resolvedSubject));

        service.createStudentWithGrades(dto, List.of(grade));

        verify(repository).save(argThat(student -> "Ivan".equals(student.getFirstName())));
        verify(gradeRepository).save(grade);
        assertSame(resolvedSubject, grade.getSubject());
        assertNotNull(grade.getStudent());
        assertEquals("Ivan", grade.getStudent().getFirstName());
    }

    @Test
    void createStudentWithGradesShouldThrowIfSubjectMissing() {
        StudentDto dto = new StudentDto(null, "Ivan", "Petrov", 10, "MALE", "ivan@mail.com");
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
