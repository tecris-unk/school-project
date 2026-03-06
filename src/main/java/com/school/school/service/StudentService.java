package com.school.school.service;

import com.school.school.service.dto.request.StudentRequest;
import com.school.school.service.dto.request.StudentWithGradesRequest;
import com.school.school.service.dto.response.StudentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudentService {

    StudentResponse createStudent(StudentRequest student);

    StudentResponse findStudentById(Long id);

    Page<StudentResponse> findStudentsByNestedFilters(
            String teacherEmail,
            String subjectName,
            Integer minScore,
            Pageable pageable,
            StudentSearchQueryType queryType
    );

    StudentResponse updateStudent(Long id, StudentRequest updatedStudent);

    void deleteStudent(Long id);

    StudentResponse createStudentWithGrades(final StudentWithGradesRequest request);
}
