package com.school.school.service;

import com.school.school.exceptions.ResourceNotFoundException;
import com.school.school.model.Student;
import com.school.school.model.Subject;
import com.school.school.repository.GradeRepository;
import com.school.school.repository.StudentRepository;
import com.school.school.repository.SubjectRepository;
import com.school.school.service.dto.request.GradeRequest;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GradeBulkTransactionIntegrationTest {

    @Autowired
    private GradeService gradeService;
    @Autowired
    private GradeRepository gradeRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private SubjectRepository subjectRepository;

    private Long existingStudentId;
    private Long existingSubjectId;

    @BeforeEach
    void setUp() {
        gradeRepository.deleteAll();
        studentRepository.deleteAll();
        subjectRepository.deleteAll();

        Student student = new Student();
        student.setFirstName("Ivan");
        student.setLastName("Ivanov");
        student.setGender(Student.Gender.MALE);
        student.setEmail("ivan@test.com");
        existingStudentId = studentRepository.save(student).getId();

        Subject subject = new Subject();
        subject.setName("Math");
        subject.setDescription("Algebra");
        existingSubjectId = subjectRepository.save(subject).getId();
    }

    @Test
    void bulkTransactional_shouldRollbackAllOnFailure() {
        GradeRequest first = new GradeRequest(9, LocalDate.now(), existingStudentId, existingSubjectId);
        GradeRequest secondBroken = new GradeRequest(7, LocalDate.now(), 999999L, existingSubjectId);

        assertThrows(ResourceNotFoundException.class,
                () -> gradeService.createGradesBulkTransactional(List.of(first, secondBroken)));

        assertEquals(0, gradeRepository.count());
    }

    @Test
    void bulkNonTransactional_shouldPersistPartiallyOnFailure() {
        GradeRequest first = new GradeRequest(9, LocalDate.now(), existingStudentId, existingSubjectId);
        GradeRequest secondBroken = new GradeRequest(7, LocalDate.now(), 999999L, existingSubjectId);

        assertThrows(ResourceNotFoundException.class,
                () -> gradeService.createGradesBulkNonTransactional(List.of(first, secondBroken)));

        assertEquals(1, gradeRepository.count());
    }
}