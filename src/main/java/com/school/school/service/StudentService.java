package com.school.school.service;

import com.school.school.service.dto.request.StudentRequest;
import com.school.school.service.dto.request.StudentWithGradesRequest;
import com.school.school.service.dto.response.StudentResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentService {

    List<StudentResponse> findAllStudentsWithGrades();

    StudentResponse createStudent(StudentRequest student);

    StudentResponse findStudentById(Long id);

    public Page<StudentResponse> findStudentsByEmailAndDate(final String email, final LocalDate date, final Pageable pageable);

    StudentResponse updateStudent(Long id, StudentRequest updatedStudent);

    void deleteStudent(Long id);

    public StudentResponse createStudentWithGradesTransactional(final StudentWithGradesRequest request);
    public StudentResponse createStudentWithGradesNoTransactional(final StudentWithGradesRequest request);
}
