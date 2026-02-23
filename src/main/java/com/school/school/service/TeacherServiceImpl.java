package com.school.school.service;

import com.school.school.model.Teacher;
import com.school.school.repository.TeacherRepository;
import com.school.school.service.dto.TeacherDto;
import com.school.school.service.mapper.TeacherMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository repository;

    private final TeacherMapper mapper;

    @Override
    public List<Teacher> findAllTeachers() {
        return repository.findAll();
    }

    @Override
    public Teacher findTeacherById(final Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void createTeacher(final TeacherDto teacherDto) {
        repository.save(mapper.toEntity(teacherDto));
    }

    @Override
    public Teacher updateTeacher(final Long id, final TeacherDto teacherDto) {
        return repository.findById(id)
                .map(existingTeacher -> {
                    mapper.updateEntity(existingTeacher, teacherDto);
                    repository.save(existingTeacher);
                    return existingTeacher;
                })
                .orElse(null);
    }

    @Override
    public boolean deleteTeacher(final Long id) {
        return repository.findById(id)
                .map(teacher -> {
                    repository.delete(teacher);
                    return true;
                })
                .orElse(false);
    }
}
