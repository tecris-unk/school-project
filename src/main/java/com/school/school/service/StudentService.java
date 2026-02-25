package com.school.school.service;

import com.school.school.model.Grade;
import com.school.school.model.Student;
import com.school.school.service.dto.StudentDto;
import java.util.List;

public interface StudentService {

    List<Student> findAllStudents();

    void createStudent(StudentDto student);

    Student findStudentById(Long id);

    Student findStudentByEmail(String email);

    Student updateStudent(Long id, StudentDto updatedStudent);

    void deleteStudent(Long id);

    void createStudentWithGrades(StudentDto student, List<Grade> grades);
}
