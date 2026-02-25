package com.school.school.service;

import com.school.school.exceptions.ResourceNotFoundException;
import com.school.school.model.Grade;
import com.school.school.model.Student;
import com.school.school.model.Subject;
import com.school.school.repository.GradeRepository;
import com.school.school.repository.StudentRepository;
import com.school.school.repository.SubjectRepository;
import com.school.school.service.dto.StudentDto;
import com.school.school.service.mapper.StudentMapper;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Primary
public class StudentServiceImpl implements StudentService {

    private static final String STUDENT_NOT_FOUND_MSG = "Student not found";
    private static final String SUBJECT_NOT_FOUND_MSG = "Subject not found";

    private final StudentRepository repository;
    private final SubjectRepository subjectRepository;
    private final GradeRepository gradeRepository;
    private final StudentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<Student> findAllStudents() {
        return repository.findAll();
    }

    @Override
    public void createStudent(final StudentDto dto) {
        repository.save(mapper.toEntity(dto));
    }

    @Override
    @Transactional
    public Student findStudentById(final Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(STUDENT_NOT_FOUND_MSG + " with id: " + id));
    }

    @Override
    @Transactional
    public Student findStudentByEmail(final String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(STUDENT_NOT_FOUND_MSG + " with email: " + email));
    }

    @Override
    @Transactional
    public boolean deleteStudent(final Long id) {
        return repository.findById(id)
                .map(user -> {
                    repository.delete(user);
                    return true;
                })
                .orElseThrow(() -> new ResourceNotFoundException(STUDENT_NOT_FOUND_MSG + " with id: " + id));
    }

    @Override
    @Transactional
    public Student updateStudent(
            final Long id,
            final StudentDto updatedStudent) {
        return repository.findById(id)
                .map(existingStudent -> {
                    mapper.updateEntity(existingStudent, updatedStudent);
                    repository.save(existingStudent);
                    return existingStudent;
                })
                .orElseThrow(() -> new ResourceNotFoundException(STUDENT_NOT_FOUND_MSG + " with id: " + id));
    }

    @Override
    @Transactional
    public void createStudentWithGrades(
            final StudentDto dto,
            final List<Grade> grades) {
        Student student = mapper.toEntity(dto);
        repository.save(student);
        for (Grade grade : grades) {
            grade.setStudent(student);
            Subject subject = subjectRepository.findById(grade.getSubject().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(SUBJECT_NOT_FOUND_MSG + " with id: " + grade.getSubject().getId()));
            grade.setSubject(subject);
            gradeRepository.save(grade);
        }
    }
}
