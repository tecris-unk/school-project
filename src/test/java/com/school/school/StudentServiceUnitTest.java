package com.school.school;

import com.school.school.model.*;
import com.school.school.repository.*;
import com.school.school.service.StudentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceUnitTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private GradeRepository gradeRepository;

    @InjectMocks
    private StudentService studentService; // предположим, что есть такой сервис

    private final Long studentId = 1L;
    private final Long subjectId = 1L;

    @Test
    void saveStudentWithGrades_withoutTransaction_shouldSaveStudentAndGradesIndependently() {
        // given
        Student student = new Student(null, "John", "Doe", 1, null, "john@test.com");
        student.setId(studentId); // после save получит id

        Subject subject = new Subject(subjectId, "Math", "Algebra", null, null);

        Grade grade1 = new Grade(null, student, subject, 5, LocalDate.now());
        Grade grade2 = new Grade(null, student, subject, 4, LocalDate.now());

        when(studentRepository.save(any(Student.class))).thenReturn(student);
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(gradeRepository.save(any(Grade.class))).thenAnswer(i -> {
            Grade g = i.getArgument(0);
            g.setId(new Random().nextLong()); // имитация присвоения ID
            return g;
        });

        // when
        studentService.saveStudentWithGradesWithTransaction(student, Arrays.asList(grade1, grade2));

        // then
        verify(studentRepository, times(1)).save(student);
        verify(subjectRepository, times(2)).findById(subjectId);
        verify(gradeRepository, times(2)).save(any(Grade.class));
    }

    @Test
    void saveStudentWithGrades_withTransaction_shouldRollbackOnError() {
        // given
        Student student = new Student(null, "Jane", "Doe", 1, null, null);
        Subject subject = new Subject(subjectId, "Math", "Algebra", null, null);

        Grade grade1 = new Grade(null, student, subject, 5, LocalDate.now());
        Grade grade2 = new Grade(null, student, subject, 4, LocalDate.now());

        when(studentRepository.save(any(Student.class))).thenReturn(student);
        when(subjectRepository.findById(subjectId))
                .thenReturn(Optional.of(subject))
                .thenThrow(new RuntimeException("DB error")); // вторая оценка вызывает ошибку

        // when / then
        assertThatThrownBy(() ->
                studentService.saveStudentWithGradesWithTransaction(student, Arrays.asList(grade1, grade2)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");

        // проверяем, что studentRepository.save вызван только раз (транзакция откатилась, но вызов был)
        verify(studentRepository, times(1)).save(student);
        verify(subjectRepository, times(2)).findById(subjectId);
        // gradeRepository.save не должен вызываться после ошибки, т.к. транзакция прервана
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    // Тесты для CRUD операций (например, создание учителя, предмета и т.д.)
    // ...
}