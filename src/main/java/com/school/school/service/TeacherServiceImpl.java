package com.school.school.service;

import com.school.school.exceptions.ResourceNotFoundException;
import com.school.school.model.Teacher;
import com.school.school.repository.TeacherRepository;
import com.school.school.service.dto.TeacherDto;
import com.school.school.service.mapper.TeacherMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private static final String TEACHER_NOT_FOUND_MSG = "Teacher not found";

    private final TeacherRepository repository;
    private final TeacherMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<Teacher> findAllTeachers() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Teacher findTeacherById(final Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TEACHER_NOT_FOUND_MSG + " with id: " + id));
    }

    @Override
    @Transactional
    public void createTeacher(final TeacherDto teacherDto) {
        repository.save(mapper.toEntity(teacherDto));
    }

    @Override
    @Transactional
    public Teacher updateTeacher(final Long id, final TeacherDto teacherDto) {
        return repository.findById(id)
                .map(existingTeacher -> {
                    mapper.updateEntity(existingTeacher, teacherDto);
                    repository.save(existingTeacher);
                    return existingTeacher;
                })
                .orElseThrow(() -> new ResourceNotFoundException(TEACHER_NOT_FOUND_MSG + " with id: " + id));
    }

    @Override
    @Transactional
    public void deleteTeacher(final Long id) {
        Teacher teacher = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TEACHER_NOT_FOUND_MSG + " with id: " + id));
        repository.delete(teacher);
    }
}
