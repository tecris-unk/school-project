package com.school.school.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.school.school.controller.mapper.TeacherMapper;
import com.school.school.exceptions.ResourceNotFoundException;
import com.school.school.model.Teacher;
import com.school.school.repository.TeacherRepository;
import com.school.school.service.dto.request.TeacherRequest;
import com.school.school.service.dto.response.TeacherResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeacherServiceImplTest {

    @Mock
    private TeacherRepository repository;
    @Mock
    private TeacherMapper mapper;
    @Mock
    private StudentSearchCacheIndex searchCacheIndex;

    @InjectMocks
    private TeacherServiceImpl teacherService;

    @Test
    void findAllTeachers_shouldMapAllEntitiesToResponses() {
        Teacher first = new Teacher();
        first.setId(1L);
        Teacher second = new Teacher();
        second.setId(2L);

        TeacherResponse firstResponse = new TeacherResponse();
        firstResponse.setId(1L);
        TeacherResponse secondResponse = new TeacherResponse();
        secondResponse.setId(2L);

        when(repository.findAll()).thenReturn(List.of(first, second));
        when(mapper.toResponse(first)).thenReturn(firstResponse);
        when(mapper.toResponse(second)).thenReturn(secondResponse);

        List<TeacherResponse> result = teacherService.findAllTeachers();

        assertEquals(2, result.size());
        assertEquals(List.of(1L, 2L), result.stream().map(TeacherResponse::getId).toList());
    }

    @Test
    void createTeacher_shouldSaveAndClearSearchCache() {
        TeacherRequest request = new TeacherRequest("Maria", "Petrova", "maria@example.com");
        Teacher teacherEntity = new Teacher();
        Teacher savedTeacher = new Teacher();
        savedTeacher.setId(10L);
        TeacherResponse response = new TeacherResponse();
        response.setId(10L);

        when(mapper.toEntity(request)).thenReturn(teacherEntity);
        when(repository.save(teacherEntity)).thenReturn(savedTeacher);
        when(mapper.toResponse(savedTeacher)).thenReturn(response);

        TeacherResponse actual = teacherService.createTeacher(request);

        assertEquals(10L, actual.getId());
        verify(searchCacheIndex, times(1)).clear();
    }

    @Test
    void updateTeacher_shouldThrowWhenTeacherNotFoundAndNotClearCache() {
        TeacherRequest request = new TeacherRequest("Maria", "Petrova", "maria@example.com");
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teacherService.updateTeacher(999L, request));

        verify(searchCacheIndex, never()).clear();
    }
}