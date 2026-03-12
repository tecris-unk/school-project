package com.school.school.service;

import com.school.school.controller.mapper.GradeMapper;
import com.school.school.controller.mapper.StudentMapper;
import com.school.school.exceptions.ConflictException;
import com.school.school.exceptions.ResourceNotFoundException;
import com.school.school.exceptions.ValidationException;
import com.school.school.model.Grade;
import com.school.school.model.SchoolClass;
import com.school.school.model.Student;
import com.school.school.repository.GradeRepository;
import com.school.school.repository.SchoolClassRepository;
import com.school.school.repository.StudentRepository;
import com.school.school.repository.SubjectRepository;
import com.school.school.service.dto.request.GradeRequest;
import com.school.school.service.dto.request.StudentRequest;
import com.school.school.service.dto.request.StudentWithGradesRequest;
import com.school.school.service.dto.response.StudentResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository repository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private SchoolClassRepository schoolClassRepository;
    @Mock
    private GradeRepository gradeRepository;
    @Mock
    private StudentMapper mapper;
    @Mock
    private GradeMapper gradeMapper;
    @Mock
    private StudentSearchCacheIndex searchCacheIndex;

    @InjectMocks
    private StudentServiceImpl studentService;

    @Test
    void createStudent_shouldThrowValidationExceptionWhenEmailBlank() {
        StudentRequest request = new StudentRequest("Ivan", "Ivanov", "MALE", " ", 1L);

        assertThrows(ValidationException.class, () -> studentService.createStudent(request));

        verify(repository, never()).save(any(Student.class));
    }

    @Test
    void createStudent_shouldThrowConflictExceptionWhenEmailAlreadyExists() {
        StudentRequest request = new StudentRequest("Ivan", "Ivanov", "MALE", "ivan@example.com", 1L);
        Student existing = new Student();
        existing.setId(777L);

        when(repository.findByEmail("ivan@example.com")).thenReturn(Optional.of(existing));

        assertThrows(ConflictException.class, () -> studentService.createStudent(request));

        verify(repository, never()).save(any(Student.class));
    }

    @Test
    void createStudent_shouldSetSchoolClassAndClearCache() {
        StudentRequest request = new StudentRequest("Ivan", "Ivanov", "MALE", "ivan@example.com", 2L);
        Student entity = new Student();
        Student saved = new Student();
        saved.setId(10L);
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(2L);
        StudentResponse response = new StudentResponse();
        response.setId(10L);

        when(repository.findByEmail("ivan@example.com")).thenReturn(Optional.empty());
        when(mapper.toEntity(request)).thenReturn(entity);
        when(schoolClassRepository.findById(2L)).thenReturn(Optional.of(schoolClass));
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        StudentResponse actual = studentService.createStudent(request);

        assertEquals(10L, actual.getId());
        assertEquals(2L, entity.getSchoolClass().getId());
        verify(searchCacheIndex, times(1)).clear();
    }

    @Test
    void createStudentWithGrades_shouldThrowWhenSubjectNotFound() {
        StudentRequest studentRequest = new StudentRequest("Ivan", "Ivanov", "MALE", "ivan@example.com", null);
        GradeRequest gradeRequest = new GradeRequest(9, LocalDate.now(), null, 100L);
        StudentWithGradesRequest request = new StudentWithGradesRequest(studentRequest, List.of(gradeRequest));

        Student studentEntity = new Student();
        Student savedStudent = new Student();
        savedStudent.setId(1L);
        Grade gradeEntity = new Grade();

        when(repository.findByEmail("ivan@example.com")).thenReturn(Optional.empty());
        when(mapper.toEntity(studentRequest)).thenReturn(studentEntity);
        when(repository.save(studentEntity)).thenReturn(savedStudent);
        when(gradeMapper.toEntity(gradeRequest)).thenReturn(gradeEntity);
        when(subjectRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.createStudentWithGrades(request));

        verify(gradeRepository, never()).save(any(Grade.class));
        verify(searchCacheIndex, never()).clear();
    }
}