package com.school.school.service;

import com.school.school.service.dto.GradeDto;
import com.school.school.service.dto.StudentDto;
import java.util.List;

public interface StudentService {

    List<StudentDto> findAllStudents();

    StudentDto createStudent(StudentDto student);

    StudentDto findStudentById(Long id);

    StudentDto findStudentByEmail(String email);

    StudentDto updateStudent(Long id, StudentDto updatedStudent);

    void deleteStudent(Long id);

    StudentDto createStudentWithGrades(StudentDto student, List<GradeDto> gradesDto);
}
