package com.school.school.service;

import com.school.school.service.dto.request.StudentRequest;
import com.school.school.service.dto.request.StudentWithGradesRequest;
import com.school.school.service.dto.response.StudentResponse;
import java.util.List;

public interface StudentService {

    List<StudentResponse> findAllStudents();

    List<StudentResponse> findAllStudentsWithGrades();

    StudentResponse createStudent(StudentRequest student);

    StudentResponse findStudentById(Long id);

    StudentResponse findStudentByEmail(String email);

    StudentResponse updateStudent(Long id, StudentRequest updatedStudent);

    void deleteStudent(Long id);

    StudentResponse createStudentWithGrades(StudentWithGradesRequest request);
}
