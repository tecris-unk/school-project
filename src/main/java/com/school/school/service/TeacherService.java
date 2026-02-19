package com.school.school.service;

import com.school.school.model.Teacher;
import com.school.school.service.dto.TeacherDTO;
import java.util.List;

public interface TeacherService {

    List<Teacher> findAllTeachers();

    Teacher findTeacherById(Long id);

    void createTeacher(TeacherDTO teacherDTO);

    Teacher updateTeacher(Long id, TeacherDTO teacherDTO);

    boolean deleteTeacher(Long id);
}
