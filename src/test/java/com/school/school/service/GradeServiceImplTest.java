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
    void findGradeById_shouldThrowWhenMissing() {
        when(gradeRepository.findById(101L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gradeService.findGradeById(101L));
    }

    @Test
    void createGrade_shouldThrowWhenStudentNotFound() {
        GradeRequest request = new GradeRequest(12, LocalDate.now(), 3L, 9L);
        when(studentRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gradeService.createGrade(request));
        verify(searchCacheIndex, never()).clear();
    }

    @Test
    void createGrade_shouldThrowWhenSubjectNotFound() {
        GradeRequest request = new GradeRequest(12, LocalDate.now(), 3L, 9L);
        Student student = new Student();
        student.setId(3L);
        when(studentRepository.findById(3L)).thenReturn(Optional.of(student));
        when(subjectRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gradeService.createGrade(request));
        verify(searchCacheIndex, never()).clear();
    }

    @Test
    void createGradesBulkNonTransactional_shouldNotClearWhenListIsEmpty() {
        List<GradeResponse> actual = gradeService.createGradesBulkNonTransactional(List.of());

        assertEquals(List.of(), actual);
        verify(searchCacheIndex, never()).clear();
    }

    @Test
    void updateGrade_shouldUpdateAndClearCache() {
        GradeRequest request = new GradeRequest(11, LocalDate.now(), 4L, 8L);
        Student student = new Student();
        student.setId(4L);
        Subject subject = new Subject();
        subject.setId(8L);
        Grade existing = new Grade();
        existing.setId(44L);
        GradeResponse response = new GradeResponse();
        response.setId(44L);

        when(studentRepository.findById(4L)).thenReturn(Optional.of(student));
        when(subjectRepository.findById(8L)).thenReturn(Optional.of(subject));
        when(gradeRepository.findById(44L)).thenReturn(Optional.of(existing));
        when(gradeRepository.save(existing)).thenReturn(existing);
        when(gradeMapper.toResponse(existing)).thenReturn(response);

        GradeResponse actual = gradeService.updateGrade(44L, request);

        assertEquals(44L, actual.getId());
        assertEquals(student, existing.getStudent());
        assertEquals(subject, existing.getSubject());
        verify(searchCacheIndex, times(1)).clear();
    }

    @Test
    void updateGrade_shouldThrowWhenStudentNotFound() {
        GradeRequest request = new GradeRequest(11, LocalDate.now(), 4L, 8L);
        when(studentRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gradeService.updateGrade(44L, request));
    }

    @Test
    void updateGrade_shouldThrowWhenSubjectNotFound() {
        GradeRequest request = new GradeRequest(11, LocalDate.now(), 4L, 8L);
        Student student = new Student();
        student.setId(4L);
        when(studentRepository.findById(4L)).thenReturn(Optional.of(student));
        when(subjectRepository.findById(8L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gradeService.updateGrade(44L, request));
    }

    @Test
    void createGradesBulkTransactional_shouldClearCacheOnSuccess() {
        GradeRequest firstRequest = new GradeRequest(8, LocalDate.now(), 1L, 10L);
        GradeRequest secondRequest = new GradeRequest(9, LocalDate.now(), 2L, 11L);

        Student firstStudent = new Student();
        firstStudent.setId(1L);
        Student secondStudent = new Student();
        secondStudent.setId(2L);
        Subject firstSubject = new Subject();
        firstSubject.setId(10L);
        Subject secondSubject = new Subject();
        secondSubject.setId(11L);

        Grade firstGrade = new Grade();
        firstGrade.setScore(8);
        Grade secondGrade = new Grade();
        secondGrade.setScore(9);

        GradeResponse firstResponse = new GradeResponse();
        firstResponse.setScore(8);
        GradeResponse secondResponse = new GradeResponse();
        secondResponse.setScore(9);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(firstStudent));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(secondStudent));
        when(subjectRepository.findById(10L)).thenReturn(Optional.of(firstSubject));
        when(subjectRepository.findById(11L)).thenReturn(Optional.of(secondSubject));
        when(gradeMapper.toEntity(firstRequest)).thenReturn(firstGrade);
        when(gradeMapper.toEntity(secondRequest)).thenReturn(secondGrade);
        when(gradeRepository.save(firstGrade)).thenReturn(firstGrade);
        when(gradeRepository.save(secondGrade)).thenReturn(secondGrade);
        when(gradeMapper.toResponse(firstGrade)).thenReturn(firstResponse);
        when(gradeMapper.toResponse(secondGrade)).thenReturn(secondResponse);

        List<GradeResponse> actual = gradeService.createGradesBulkTransactional(List.of(firstRequest, secondRequest));

        assertEquals(2, actual.size());
        assertEquals(List.of(8, 9), actual.stream().map(GradeResponse::getScore).toList());
        verify(searchCacheIndex, times(1)).clear();
    }

    @Test
    void createGradesBulkNonTransactional_shouldNotClearWhenFirstItemFails() {
        GradeRequest broken = new GradeRequest(7, LocalDate.now(), 999L, 10L);
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> gradeService.createGradesBulkNonTransactional(List.of(broken))
        );

        verify(searchCacheIndex, never()).clear();
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void deleteGrade_shouldThrowWhenMissing() {
        when(gradeRepository.findById(18L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gradeService.deleteGrade(18L));
        verify(searchCacheIndex, never()).clear();
    }

    @Test
    void createGradesBulkTransactional_shouldRollbackOnErrorAndStopSavingNextEntries() {
        GradeRequest ok = new GradeRequest(8, LocalDate.now(), 1L, 10L);
        GradeRequest broken = new GradeRequest(7, LocalDate.now(), 999L, 10L);
        List<GradeRequest> requests = List.of(ok, broken);

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

        assertThrows(ResourceNotFoundException.class, () -> gradeService.createGradesBulkTransactional(requests));

        verify(gradeRepository, times(1)).save(any(Grade.class));
        verify(searchCacheIndex, never()).clear();
    }

    @Test
    void createGradesBulkNonTransactional_shouldKeepFirstSuccessWhenSecondFails() {
        GradeRequest ok = new GradeRequest(8, LocalDate.now(), 1L, 10L);
        GradeRequest broken = new GradeRequest(7, LocalDate.now(), 999L, 10L);

        List<GradeRequest> requests = List.of(ok, broken);

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
                () -> gradeService.createGradesBulkNonTransactional(requests));

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

    @Test
    void createGrade_shouldAssignStudentAndSubjectAndClearCache() {
        GradeRequest request = new GradeRequest(12, LocalDate.now(), 3L, 9L);
        Student student = new Student();
        student.setId(3L);
        Subject subject = new Subject();
        subject.setId(9L);
        Grade grade = new Grade();
        GradeResponse response = new GradeResponse();
        response.setId(22L);

        when(studentRepository.findById(3L)).thenReturn(Optional.of(student));
        when(subjectRepository.findById(9L)).thenReturn(Optional.of(subject));
        when(gradeMapper.toEntity(request)).thenReturn(grade);
        when(gradeRepository.save(grade)).thenReturn(grade);
        when(gradeMapper.toResponse(grade)).thenReturn(response);

        GradeResponse actual = gradeService.createGrade(request);

        assertEquals(22L, actual.getId());
        assertEquals(student, grade.getStudent());
        assertEquals(subject, grade.getSubject());
        verify(searchCacheIndex, times(1)).clear();
    }

    @Test
    void updateGrade_shouldThrowWhenGradeNotFoundAndNotClearCache() {
        GradeRequest request = new GradeRequest(11, LocalDate.now(), 4L, 8L);
        Student student = new Student();
        student.setId(4L);
        Subject subject = new Subject();
        subject.setId(8L);

        when(studentRepository.findById(4L)).thenReturn(Optional.of(student));
        when(subjectRepository.findById(8L)).thenReturn(Optional.of(subject));
        when(gradeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gradeService.updateGrade(99L, request));

        verify(searchCacheIndex, never()).clear();
    }

    @Test
    void findGradeById_shouldMapFoundEntity() {
        Grade grade = new Grade();
        grade.setId(14L);
        GradeResponse response = new GradeResponse();
        response.setId(14L);

        when(gradeRepository.findById(14L)).thenReturn(Optional.of(grade));
        when(gradeMapper.toResponse(grade)).thenReturn(response);

        GradeResponse actual = gradeService.findGradeById(14L);

        assertEquals(14L, actual.getId());
    }

    @Test
    void deleteGrade_shouldDeleteEntityAndClearCache() {
        Grade grade = new Grade();
        grade.setId(18L);

        when(gradeRepository.findById(18L)).thenReturn(Optional.of(grade));

        gradeService.deleteGrade(18L);

        verify(gradeRepository, times(1)).delete(grade);
        verify(searchCacheIndex, times(1)).clear();
    }
}
