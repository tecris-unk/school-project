package com.school.school.service;

import com.school.school.model.Student;
import com.school.school.service.dto.StudentDTO;
import java.util.List;

public interface StudentService {
    List<Student> findAllStudents();

    Student createStudent(StudentDTO student);

    Student findStudentById(Long id);

    Student updateStudent(Long id, StudentDTO updatedStudent);

    void deleteStudent(Long id);
}
