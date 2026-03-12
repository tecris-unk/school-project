package com.school.school.service;

import com.school.school.controller.mapper.GradeMapper;
import com.school.school.exceptions.ResourceNotFoundException;
import com.school.school.model.Grade;
import com.school.school.model.Student;
import com.school.school.model.Subject;
import com.school.school.repository.GradeRepository;
import com.school.school.repository.StudentRepository;
import com.school.school.repository.SubjectRepository;
import com.school.school.service.dto.request.GradeRequest;
import com.school.school.service.dto.response.GradeResponse;
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
class GradeServiceImplTest {

    @Mock
    private GradeRepository gradeRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private GradeMapper gradeMapper;
    @Mock
    private StudentSearchCacheIndex searchCacheIndex;

    @InjectMocks
    private GradeServiceImpl gradeService;

    @Test
    void createGradesBulkTransactional_shouldRollbackOnErrorAndStopSavingNextEntries() {
        GradeRequest ok = new GradeRequest(8, LocalDate.now(), 1L, 10L);
        GradeRequest broken = new GradeRequest(7, LocalDate.now(), 999L, 10L);

        Student student = new Student();
        student.setId(1L);
        Subject subject = new Subject();
        subject.setId(10L);

        Grade grade = new Grade();
        grade.setScore(8);
        GradeResponse response = new GradeResponse();
        response.setScore(8);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());
        when(subjectRepository.findById(10L)).thenReturn(Optional.of(subject));
        when(gradeMapper.toEntity(ok)).thenReturn(grade);
        when(gradeRepository.save(any(Grade.class))).thenReturn(grade);
        when(gradeMapper.toResponse(grade)).thenReturn(response);

        assertThrows(ResourceNotFoundException.class,
                () -> gradeService.createGradesBulkTransactional(List.of(ok, broken)));

        verify(gradeRepository, times(1)).save(any(Grade.class));
        verify(searchCacheIndex, never()).clear();
    }

    @Test
    void createGradesBulkNonTransactional_shouldKeepFirstSuccessWhenSecondFails() {
        GradeRequest ok = new GradeRequest(8, LocalDate.now(), 1L, 10L);
        GradeRequest broken = new GradeRequest(7, LocalDate.now(), 999L, 10L);

        Student student = new Student();
        student.setId(1L);
        Subject subject = new Subject();
        subject.setId(10L);

        Grade grade = new Grade();
        grade.setScore(8);
        GradeResponse response = new GradeResponse();
        response.setScore(8);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());
        when(subjectRepository.findById(10L)).thenReturn(Optional.of(subject));
        when(gradeMapper.toEntity(ok)).thenReturn(grade);
        when(gradeRepository.save(any(Grade.class))).thenReturn(grade);
        when(gradeMapper.toResponse(grade)).thenReturn(response);

        assertThrows(ResourceNotFoundException.class,
                () -> gradeService.createGradesBulkNonTransactional(List.of(ok, broken)));

        verify(gradeRepository, times(1)).save(any(Grade.class));
        verify(searchCacheIndex, times(1)).clear();
    }

    @Test
    void findAllGrades_shouldUseStreamMapping() {
        Grade first = new Grade();
        first.setId(1L);
        Grade second = new Grade();
        second.setId(2L);

        GradeResponse firstResponse = new GradeResponse();
        firstResponse.setId(1L);
        GradeResponse secondResponse = new GradeResponse();
        secondResponse.setId(2L);

        when(gradeRepository.findAll()).thenReturn(List.of(first, second));
        when(gradeMapper.toResponse(first)).thenReturn(firstResponse);
        when(gradeMapper.toResponse(second)).thenReturn(secondResponse);

        List<GradeResponse> responses = gradeService.findAllGrades();

        assertEquals(2, responses.size());
        assertEquals(List.of(1L, 2L), responses.stream().map(GradeResponse::getId).toList());
    }
}