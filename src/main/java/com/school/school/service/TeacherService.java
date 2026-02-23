package com.school.school.service;

import com.school.school.model.Teacher;
import com.school.school.service.dto.TeacherDto;
import java.util.List;

public interface TeacherService {

    List<Teacher> findAllTeachers();

    Teacher findTeacherById(Long id);

    void createTeacher(TeacherDto teacherDto);

    Teacher updateTeacher(Long id, TeacherDto teacherDto);

    boolean deleteTeacher(Long id);
}
