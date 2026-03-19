package com.school.school.service;

import com.school.school.controller.mapper.SchoolClassMapper;
import com.school.school.exceptions.ResourceNotFoundException;
import com.school.school.model.SchoolClass;
import com.school.school.model.Subject;
import com.school.school.repository.SchoolClassRepository;
import com.school.school.repository.SubjectRepository;
import com.school.school.service.dto.request.SchoolClassRequest;
import com.school.school.service.dto.response.SchoolClassResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchoolClassServiceImplTest {

    @Mock
    private SchoolClassRepository repository;
    @Mock
    private SchoolClassMapper mapper;
    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private SchoolClassServiceImpl schoolClassService;

    @Test
    void addSubjectToClass_shouldNotDuplicateSubject() {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(1L);
        Subject existingSubject = new Subject();
        existingSubject.setId(10L);
        schoolClass.setSubjects(new ArrayList<>());
        schoolClass.getSubjects().add(existingSubject);

        SchoolClassResponse response = new SchoolClassResponse();
        response.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(schoolClass));
        when(subjectRepository.findById(10L)).thenReturn(Optional.of(existingSubject));
        when(repository.save(schoolClass)).thenReturn(schoolClass);
        when(mapper.toResponse(schoolClass)).thenReturn(response);

        SchoolClassResponse actual = schoolClassService.addSubjectToClass(1L, 10L);

        assertEquals(1L, actual.getId());
        assertEquals(1, schoolClass.getSubjects().size());
    }

    @Test
    void addSubjectToClass_shouldThrowWhenClassNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> schoolClassService.addSubjectToClass(999L, 1L));

        verify(subjectRepository, never()).findById(1L);
    }

    @Test
    void deleteClass_shouldThrowWhenClassNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> schoolClassService.deleteClass(999L));
    }
    @Test
    void findAllSchoolClassesWithSubjects_shouldMapRepositoryResult() {
        SchoolClass first = new SchoolClass();
        first.setId(1L);
        SchoolClass second = new SchoolClass();
        second.setId(2L);

        SchoolClassResponse firstResponse = new SchoolClassResponse();
        firstResponse.setId(1L);
        SchoolClassResponse secondResponse = new SchoolClassResponse();
        secondResponse.setId(2L);

        when(repository.findAllWithSubjectsBy()).thenReturn(List.of(first, second));
        when(mapper.toResponse(first)).thenReturn(firstResponse);
        when(mapper.toResponse(second)).thenReturn(secondResponse);

        List<SchoolClassResponse> actual = schoolClassService.findAllSchoolClassesWithSubjects();

        assertEquals(List.of(1L, 2L), actual.stream().map(SchoolClassResponse::getId).toList());
    }

    @Test
    void updateClass_shouldSaveMappedEntity() {
        SchoolClassRequest request = new SchoolClassRequest(10, "A");
        SchoolClass existing = new SchoolClass();
        existing.setId(8L);
        SchoolClassResponse response = new SchoolClassResponse();
        response.setId(8L);

        when(repository.findById(8L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toResponse(existing)).thenReturn(response);

        SchoolClassResponse actual = schoolClassService.updateClass(8L, request);

        assertEquals(8L, actual.getId());
        verify(mapper).updateEntity(existing, request);
        verify(repository).save(existing);
    }

    @Test
    void createClass_shouldMapSavedEntity() {
        SchoolClassRequest request = new SchoolClassRequest(7, "B");
        SchoolClass entity = new SchoolClass();
        SchoolClass saved = new SchoolClass();
        saved.setId(12L);
        SchoolClassResponse response = new SchoolClassResponse();
        response.setId(12L);

        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        SchoolClassResponse actual = schoolClassService.createClass(request);

        assertEquals(12L, actual.getId());
    }

    @Test
    void deleteClass_shouldDeleteFoundEntity() {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(4L);

        when(repository.findById(4L)).thenReturn(Optional.of(schoolClass));

        schoolClassService.deleteClass(4L);

        verify(repository).delete(schoolClass);
    }
}
