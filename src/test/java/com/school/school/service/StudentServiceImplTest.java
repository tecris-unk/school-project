package com.school.school.service;

import com.school.school.controller.mapper.GradeMapper;
import com.school.school.controller.mapper.StudentMapper;
import com.school.school.exceptions.ConflictException;
import com.school.school.exceptions.ResourceNotFoundException;
import com.school.school.exceptions.ValidationException;
import com.school.school.model.Grade;
import com.school.school.model.SchoolClass;
import com.school.school.model.Student;
import com.school.school.model.Subject;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @Test
    void updateStudent_shouldUpdateEntityAndClearCache() {
        StudentRequest request = new StudentRequest("Ivan", "Sidorov", "MALE", "ivan@example.com", 4L);
        Student existing = new Student();
        existing.setId(15L);
        existing.setEmail("ivan@example.com");
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(4L);
        StudentResponse response = new StudentResponse();
        response.setId(15L);

        when(repository.findById(15L)).thenReturn(Optional.of(existing));
        when(repository.findByEmail("ivan@example.com")).thenReturn(Optional.of(existing));
        when(schoolClassRepository.findById(4L)).thenReturn(Optional.of(schoolClass));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toResponse(existing)).thenReturn(response);

        StudentResponse actual = studentService.updateStudent(15L, request);

        assertEquals(15L, actual.getId());
        assertEquals(4L, existing.getSchoolClass().getId());
        verify(mapper).updateEntity(existing, request);
        verify(searchCacheIndex, times(1)).clear();
    }

    @Test
    void findStudentById_shouldMapFoundEntity() {
        Student student = new Student();
        student.setId(6L);
        StudentResponse response = new StudentResponse();
        response.setId(6L);

        when(repository.findById(6L)).thenReturn(Optional.of(student));
        when(mapper.toResponse(student)).thenReturn(response);

        StudentResponse actual = studentService.findStudentById(6L);

        assertEquals(6L, actual.getId());
    }

    @Test
    void createStudent_shouldAllowNullSchoolClass() {
        StudentRequest request = new StudentRequest("Ivan", "Ivanov", "MALE", "ivan@example.com", null);
        Student entity = new Student();
        Student saved = new Student();
        saved.setId(10L);
        StudentResponse response = new StudentResponse();
        response.setId(10L);

        when(repository.findByEmail("ivan@example.com")).thenReturn(Optional.empty());
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        StudentResponse actual = studentService.createStudent(request);

        assertEquals(10L, actual.getId());
        verify(schoolClassRepository, never()).findById(any());
    }

    @Test
    void createStudentWithGrades_shouldPersistGradeAndClearCache() {
        StudentRequest studentRequest = new StudentRequest("Ivan", "Ivanov", "MALE", "ivan@example.com", 3L);
        GradeRequest gradeRequest = new GradeRequest(10, LocalDate.now(), null, 20L);
        StudentWithGradesRequest request = new StudentWithGradesRequest(studentRequest, List.of(gradeRequest));

        Student studentEntity = new Student();
        Student savedStudent = new Student();
        savedStudent.setId(7L);
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(3L);
        Grade gradeEntity = new Grade();
        Subject subject = new Subject();
        subject.setId(20L);
        StudentResponse response = new StudentResponse();
        response.setId(7L);

        when(repository.findByEmail("ivan@example.com")).thenReturn(Optional.empty());
        when(mapper.toEntity(studentRequest)).thenReturn(studentEntity);
        when(schoolClassRepository.findById(3L)).thenReturn(Optional.of(schoolClass));
        when(repository.save(studentEntity)).thenReturn(savedStudent);
        when(gradeMapper.toEntity(gradeRequest)).thenReturn(gradeEntity);
        when(subjectRepository.findById(20L)).thenReturn(Optional.of(subject));
        when(mapper.toResponse(savedStudent)).thenReturn(response);

        StudentResponse actual = studentService.createStudentWithGrades(request);

        assertEquals(7L, actual.getId());
        verify(gradeRepository).save(gradeEntity);
        assertEquals(savedStudent, gradeEntity.getStudent());
        assertEquals(subject, gradeEntity.getSubject());
        verify(searchCacheIndex, times(1)).clear();
    }
    @Test
    void findStudentsByNestedFilters_shouldReturnCachedPageWhenPresent() {
        Pageable pageable = PageRequest.of(0, 10);
        StudentResponse response = new StudentResponse();
        response.setId(1L);
        Page<StudentResponse> cachedPage = new PageImpl<>(List.of(response), pageable, 1);

        when(searchCacheIndex.get(any())).thenReturn(cachedPage);

        Page<StudentResponse> actual = studentService.findStudentsByNestedFilters(
                "teacher@example.com",
                " Math ",
                7,
                pageable,
                StudentSearchQueryType.JPQL
        );

        assertEquals(1, actual.getTotalElements());
        verify(repository, never()).findStudentIdsByNestedFiltersJpql(any(), any(), any(), any());
        verify(searchCacheIndex, never()).put(any(), any());
    }

    @Test
    void findStudentsByNestedFilters_shouldQueryRepositoryAndCacheOrderedNativeResult() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Long> idsPage = new PageImpl<>(List.of(2L, 1L), pageable, 2);
        Student firstStudent = new Student();
        firstStudent.setId(1L);
        Student secondStudent = new Student();
        secondStudent.setId(2L);
        StudentResponse firstResponse = new StudentResponse();
        firstResponse.setId(1L);
        StudentResponse secondResponse = new StudentResponse();
        secondResponse.setId(2L);

        when(searchCacheIndex.get(any())).thenReturn(null);
        when(repository.findStudentIdsByNestedFiltersNative("teacher@example.com", "math", 8, pageable))
                .thenReturn(idsPage);
        when(repository.findAllByIdsWithGradesAndTeacher(List.of(2L, 1L)))
                .thenReturn(List.of(firstStudent, secondStudent));
        when(mapper.toResponse(firstStudent)).thenReturn(firstResponse);
        when(mapper.toResponse(secondStudent)).thenReturn(secondResponse);

        Page<StudentResponse> actual = studentService.findStudentsByNestedFilters(
                "teacher@example.com",
                " Math ",
                8,
                pageable,
                StudentSearchQueryType.NATIVE
        );

        assertEquals(List.of(2L, 1L), actual.getContent().stream().map(StudentResponse::getId).toList());
        verify(searchCacheIndex, times(1)).put(any(), any());
    }

    @Test
    void findStudentById_shouldThrowWhenMissing() {
        when(repository.findById(6L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.findStudentById(6L));
    }

    @Test
    void findStudentsByNestedFilters_shouldCacheEmptyPageWhenNoIdsFound() {
        Pageable pageable = PageRequest.of(1, 5);
        Page<Long> idsPage = new PageImpl<>(List.of(), pageable, 0);

        when(searchCacheIndex.get(any())).thenReturn(null);
        when(repository.findStudentIdsByNestedFiltersJpql(null, null, null, pageable)).thenReturn(idsPage);

        Page<StudentResponse> actual = studentService.findStudentsByNestedFilters(
                null,
                " ",
                null,
                pageable,
                StudentSearchQueryType.JPQL
        );

        assertEquals(0, actual.getTotalElements());
        verify(repository, never()).findAllByIdsWithGradesAndTeacher(any());
        verify(searchCacheIndex, times(1)).put(any(), any());
    }

    @Test
    void updateStudent_shouldThrowWhenEmailOwnedByAnotherStudent() {
        StudentRequest request = new StudentRequest("Ivan", "Sidorov", "MALE", "ivan@example.com", null);
        Student existing = new Student();
        existing.setId(15L);
        Student another = new Student();
        another.setId(16L);
        when(repository.findById(15L)).thenReturn(Optional.of(existing));
        when(repository.findByEmail("ivan@example.com")).thenReturn(Optional.of(another));

        assertThrows(ConflictException.class, () -> studentService.updateStudent(15L, request));
        verify(searchCacheIndex, never()).clear();
    }

    @Test
    void deleteStudent_shouldDeleteAndClearCache() {
        Student student = new Student();
        student.setId(5L);
        when(repository.findById(5L)).thenReturn(Optional.of(student));

        studentService.deleteStudent(5L);

        verify(repository).delete(student);
        verify(searchCacheIndex).clear();
    }

    @Test
    void deleteStudent_shouldThrowWhenNotFound() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.deleteStudent(5L));
        verify(searchCacheIndex, never()).clear();
    }
}
