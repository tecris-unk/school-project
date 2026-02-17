package com.school.school.service;

import com.school.school.model.Grade;
import com.school.school.model.Student;
import com.school.school.model.Subject;
import com.school.school.repository.GradeRepository;
import com.school.school.repository.StudentRepository;
import com.school.school.repository.SubjectRepository;
import com.school.school.service.dto.StudentDTO;
import com.school.school.service.mapper.StudentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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
    private StudentMapper mapper;

    @InjectMocks
    private StudentServiceImpl service;

    @Test
    void createStudentShouldMapAndSaveEntity() {
        StudentDTO dto = new StudentDTO(null, "Ivan", "Petrov", 10, "MALE", "ivan@mail.com");
        Student student = new Student();
        when(mapper.toEntity(dto)).thenReturn(student);

        Student result = service.createStudent(dto);

        assertSame(student, result);
        verify(repository).save(student);
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
        StudentDTO dto = new StudentDTO();
        when(repository.findById(2L)).thenReturn(Optional.of(existing));

        Student result = service.updateStudent(2L, dto);

        assertSame(existing, result);
        verify(mapper).updateEntity(existing, dto);
        verify(repository).save(existing);
    }

    @Test
    void saveStudentWithGradesWithTransactionShouldSetStudentAndManagedSubject() {
        Student student = new Student(11L, "A", "B", 8, Student.Gender.MALE, "a@mail.com");
        Subject gradeSubjectRef = new Subject();
        gradeSubjectRef.setId(5L);

        Grade grade = new Grade();
        grade.setSubject(gradeSubjectRef);

        Subject managedSubject = new Subject();
        managedSubject.setId(5L);
        when(subjectRepository.findById(5L)).thenReturn(Optional.of(managedSubject));

        service.saveStudentWithGradesWithTransaction(student, List.of(grade));

        verify(repository).save(student);
        ArgumentCaptor<Grade> captor = ArgumentCaptor.forClass(Grade.class);
        verify(gradeRepository).save(captor.capture());

        Grade savedGrade = captor.getValue();
        assertAll(
                () -> assertSame(student, savedGrade.getStudent()),
                () -> assertSame(managedSubject, savedGrade.getSubject())
        );
    }

    @Test
    void saveStudentWithGradesWithTransactionShouldThrowIfSubjectMissing() {
        Student student = new Student();
        Subject gradeSubjectRef = new Subject();
        gradeSubjectRef.setId(404L);

        Grade grade = new Grade();
        grade.setSubject(gradeSubjectRef);

        when(subjectRepository.findById(404L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.saveStudentWithGradesWithTransaction(student, List.of(grade)));

        assertEquals("Subject not found", ex.getMessage());
        verify(gradeRepository, never()).save(any());
    }
}