package com.school.school.service;

import com.school.school.exceptions.ResourceNotFoundException;
import com.school.school.model.Teacher;
import com.school.school.repository.TeacherRepository;
import com.school.school.service.dto.request.TeacherRequest;
import com.school.school.controller.mapper.TeacherMapper;
import com.school.school.service.dto.response.TeacherResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private static final String TEACHER_NOT_FOUND_MSG = "Teacher not found";

    private static final String WITH_ID = " with id: ";

    private final TeacherRepository repository;
    private final TeacherMapper mapper;
    private final StudentSearchCacheIndex searchCacheIndex;

    @Override
    @Transactional(readOnly = true)
    public List<TeacherResponse> findAllTeachers() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherResponse findTeacherById(final Long id) {
        Teacher teacher = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TEACHER_NOT_FOUND_MSG + WITH_ID + id));
        return mapper.toResponse(teacher);
    }

    @Override
    @Transactional
    public TeacherResponse createTeacher(final TeacherRequest teacherRequest) {
        Teacher teacher = repository.save(mapper.toEntity(teacherRequest));
        searchCacheIndex.clear();
        return mapper.toResponse(teacher);
    }

    @Override
    @Transactional
    public TeacherResponse updateTeacher(final Long id, final TeacherRequest teacherRequest) {
        Teacher teacher = repository.findById(id)
                .map(existingTeacher -> {
                    mapper.updateEntity(existingTeacher, teacherRequest);
                    return repository.save(existingTeacher);
                })
                .orElseThrow(() -> new ResourceNotFoundException(TEACHER_NOT_FOUND_MSG + WITH_ID + id));
        searchCacheIndex.clear();
        return mapper.toResponse(teacher);
    }

    @Override
    @Transactional
    public void deleteTeacher(final Long id) {
        Teacher teacher = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TEACHER_NOT_FOUND_MSG + WITH_ID + id));
        searchCacheIndex.clear();
        repository.delete(teacher);
    }
}
