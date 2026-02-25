package com.school.school.service;

import com.school.school.service.dto.TeacherDto;
import java.util.List;

public interface TeacherService {

    List<TeacherDto> findAllTeachers();

    TeacherDto findTeacherById(Long id);

    TeacherDto createTeacher(TeacherDto teacherDto);

    TeacherDto updateTeacher(Long id, TeacherDto teacherDto);

    void deleteTeacher(Long id);
}
