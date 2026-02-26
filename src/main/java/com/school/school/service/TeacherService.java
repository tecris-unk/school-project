package com.school.school.service;

import com.school.school.service.dto.request.TeacherRequest;
import com.school.school.service.dto.response.TeacherResponse;
import java.util.List;

public interface TeacherService {

    List<TeacherResponse> findAllTeachers();

    TeacherResponse findTeacherById(Long id);

    TeacherResponse createTeacher(TeacherRequest teacherRequest);

    TeacherResponse updateTeacher(Long id, TeacherRequest teacherRequest);

    void deleteTeacher(Long id);
}
