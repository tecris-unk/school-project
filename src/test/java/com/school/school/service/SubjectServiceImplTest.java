package com.school.school.service;

import com.school.school.controller.mapper.SubjectMapper;
import com.school.school.exceptions.ResourceNotFoundException;
import com.school.school.model.Subject;
import com.school.school.model.Teacher;
import com.school.school.repository.SubjectRepository;
import com.school.school.repository.TeacherRepository;
import com.school.school.service.cache.StudentSearchCacheIndex;
import com.school.school.service.dto.request.SubjectRequest;
import com.school.school.service.dto.response.SubjectResponse;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubjectServiceImplTest {

    @Mock
    private SubjectRepository repository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private SubjectMapper mapper;
    @Mock
    private StudentSearchCacheIndex searchCacheIndex;

    @InjectMocks
    private SubjectServiceImpl subjectService;

    @Test
    void createSubject_shouldSetTeacherAndClearCache() {
        SubjectRequest request = new SubjectRequest("Math", "Algebra", 5L);
        Teacher teacher = new Teacher();
        teacher.setId(5L);
        Subject subject = new Subject();
        Subject saved = new Subject();
        saved.setId(1L);
        SubjectResponse response = new SubjectResponse();
        response.setId(1L);

        when(mapper.toEntity(request)).thenReturn(subject);
        when(teacherRepository.findById(5L)).thenReturn(Optional.of(teacher));
        when(repository.save(subject)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        SubjectResponse actual = subjectService.createSubject(request);

        assertEquals(1L, actual.getId());
        assertEquals(5L, subject.getTeacher().getId());
        verify(searchCacheIndex, times(1)).clear();
    }

    @Test
    void findAllSubjects_shouldMapAll() {
        Subject first = new Subject();
        first.setId(1L);
        SubjectResponse firstResponse = new SubjectResponse();
        firstResponse.setId(1L);
        when(repository.findAll()).thenReturn(List.of(first));
        when(mapper.toResponse(first)).thenReturn(firstResponse);

        List<SubjectResponse> actual = subjectService.findAllSubjects();

        assertEquals(List.of(1L), actual.stream().map(SubjectResponse::getId).toList());
    }

    @Test
    void createSubject_shouldSaveWithoutTeacherWhenTeacherIdMissing() {
        SubjectRequest request = new SubjectRequest("Math", "Algebra", null);
        Subject subject = new Subject();
        Subject saved = new Subject();
        saved.setId(1L);
        SubjectResponse response = new SubjectResponse();
        response.setId(1L);

        when(mapper.toEntity(request)).thenReturn(subject);
        when(repository.save(subject)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        SubjectResponse actual = subjectService.createSubject(request);

        assertEquals(1L, actual.getId());
        assertNull(subject.getTeacher());
        verify(teacherRepository, never()).findById(anyLong());
        verify(searchCacheIndex, times(1)).clear();
    }

    @Test
    void createSubject_shouldThrowWhenTeacherNotFoundAndNotClearCache() {
        SubjectRequest request = new SubjectRequest("Math", "Algebra", 999L);
        Subject subject = new Subject();

        when(mapper.toEntity(request)).thenReturn(subject);
        when(teacherRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> subjectService.createSubject(request));

        verify(searchCacheIndex, never()).clear();
    }

    @Test
    void updateSubject_shouldRemoveTeacherWhenTeacherIdIsNull() {
        SubjectRequest request = new SubjectRequest("Math", "Geometry", null);
        Subject existing = new Subject();
        Teacher oldTeacher = new Teacher();
        oldTeacher.setId(7L);
        existing.setTeacher(oldTeacher);

        SubjectResponse response = new SubjectResponse();
        response.setId(3L);

        when(repository.findById(3L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toResponse(existing)).thenReturn(response);

        SubjectResponse actual = subjectService.updateSubject(3L, request);

        assertEquals(3L, actual.getId());
        assertNull(existing.getTeacher());
        verify(searchCacheIndex, times(1)).clear();
    }

    @Test
    void findSubjectById_shouldMapFoundEntity() {
        Subject subject = new Subject();
        subject.setId(2L);
        SubjectResponse response = new SubjectResponse();
        response.setId(2L);

        when(repository.findById(2L)).thenReturn(Optional.of(subject));
        when(mapper.toResponse(subject)).thenReturn(response);

        SubjectResponse actual = subjectService.findSubjectById(2L);

        assertEquals(2L, actual.getId());
    }

    @Test
    void findSubjectById_shouldThrowWhenNotFound() {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> subjectService.findSubjectById(2L));
    }

    @Test
    void updateSubject_shouldAssignTeacherAndClearCache() {
        SubjectRequest request = new SubjectRequest("Math", "Advanced algebra", 11L);
        Subject existing = new Subject();
        existing.setId(4L);
        Teacher teacher = new Teacher();
        teacher.setId(11L);
        SubjectResponse response = new SubjectResponse();
        response.setId(4L);

        when(repository.findById(4L)).thenReturn(Optional.of(existing));
        when(teacherRepository.findById(11L)).thenReturn(Optional.of(teacher));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toResponse(existing)).thenReturn(response);

        SubjectResponse actual = subjectService.updateSubject(4L, request);

        assertEquals(4L, actual.getId());
        assertEquals(11L, existing.getTeacher().getId());
        verify(searchCacheIndex, times(1)).clear();
    }

    @Test
    void updateSubject_shouldThrowWhenTeacherNotFound() {
        SubjectRequest request = new SubjectRequest("Math", "Advanced algebra", 11L);
        Subject existing = new Subject();
        existing.setId(4L);

        when(repository.findById(4L)).thenReturn(Optional.of(existing));
        when(teacherRepository.findById(11L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> subjectService.updateSubject(4L, request));
        verify(searchCacheIndex, never()).clear();
    }

    @Test
    void updateSubject_shouldThrowWhenSubjectNotFound() {
        SubjectRequest request = new SubjectRequest("Math", "Advanced algebra", 11L);
        when(repository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> subjectService.updateSubject(4L, request));
        verify(searchCacheIndex, never()).clear();
    }

    @Test
    void deleteSubject_shouldDeleteEntityAndClearCache() {
        Subject subject = new Subject();
        subject.setId(9L);

        when(repository.findById(9L)).thenReturn(Optional.of(subject));

        subjectService.deleteSubject(9L);

        verify(repository).delete(subject);
        verify(searchCacheIndex, times(1)).clear();
    }

    @Test
    void deleteSubject_shouldThrowWhenNotFound() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> subjectService.deleteSubject(9L));
        verify(searchCacheIndex, never()).clear();
    }
}
