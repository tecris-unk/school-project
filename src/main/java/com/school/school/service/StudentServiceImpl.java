package com.school.school.service;

import com.school.school.exceptions.ConflictException;
import com.school.school.exceptions.ResourceNotFoundException;
import com.school.school.exceptions.ValidationException;
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
    private static final String EMAIL_ALREADY_EXISTS_MSG = "Student with this email already exists";
    private static final String EMAIL_REQUIRED_MSG = "Email must not be blank";

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
        validateEmail(dto.getEmail());
        ensureEmailUnique(dto.getEmail());
        repository.save(mapper.toEntity(dto));
    }

    @Override
    @Transactional(readOnly = true)
    public Student findStudentById(final Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(STUDENT_NOT_FOUND_MSG + " with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Student findStudentByEmail(final String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(STUDENT_NOT_FOUND_MSG + " with email: " + email));
    }

    @Override
    @Transactional
    public void deleteStudent(final Long id) {
        Student student = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(STUDENT_NOT_FOUND_MSG + " with id: " + id));
        repository.delete(student);
    }

    @Override
    @Transactional
    public Student updateStudent(
            final Long id,
            final StudentDto updatedStudent) {
        validateEmail(updatedStudent.getEmail());
        return repository.findById(id)
                .map(existingStudent -> {
                    ensureEmailUniqueForUpdate(updatedStudent.getEmail(), existingStudent.getId());
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

    private void validateEmail(final String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException(EMAIL_REQUIRED_MSG);
        }
    }

    private void ensureEmailUnique(final String email) {
        if (repository.findByEmail(email).isPresent()) {
            throw new ConflictException(EMAIL_ALREADY_EXISTS_MSG + ": " + email);
        }
    }

    private void ensureEmailUniqueForUpdate(final String email, final Long currentStudentId) {
        repository.findByEmail(email)
                .filter(student -> !student.getId().equals(currentStudentId))
                .ifPresent(student -> {
                    throw new ConflictException(EMAIL_ALREADY_EXISTS_MSG + ": " + email);
                });
    }
}
