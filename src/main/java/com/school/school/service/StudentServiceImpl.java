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
import com.school.school.service.dto.GradeDto;
import com.school.school.service.dto.StudentDto;
import com.school.school.service.mapper.GradeMapper;
import com.school.school.service.mapper.StudentMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final GradeMapper gradeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<StudentDto> findAllStudents() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public StudentDto createStudent(final StudentDto dto) {
        validateEmail(dto.getEmail());
        ensureEmailUnique(dto.getEmail());
        repository.save(mapper.toEntity(dto));
        Student saved = repository.save(mapper.toEntity(dto));
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDto findStudentById(final Long id) {
        Student student = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(STUDENT_NOT_FOUND_MSG + " with id: " + id));
        return mapper.toDto(student);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDto findStudentByEmail(final String email) {
        Student student = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(STUDENT_NOT_FOUND_MSG + " with email: " + email));
        return mapper.toDto(student);
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
    public StudentDto updateStudent(
            final Long id,
            final StudentDto updatedStudent) {
        validateEmail(updatedStudent.getEmail());
        Student student = repository.findById(id)
                .map(existingStudent -> {
                    ensureEmailUniqueForUpdate(updatedStudent.getEmail(), existingStudent.getId());
                    mapper.updateEntity(existingStudent, updatedStudent);
                    return repository.save(existingStudent);
                })
                .orElseThrow(() -> new ResourceNotFoundException(STUDENT_NOT_FOUND_MSG + " with id: " + id));
        return mapper.toDto(student);
    }

    @Override
    @Transactional
    public StudentDto createStudentWithGrades(
            final StudentDto dto,
            final List<GradeDto> grades) {
        Student student = repository.save(mapper.toEntity(dto));
        for (GradeDto gradeDto : grades) {
            Grade grade = gradeMapper.toEntity(gradeDto);
            grade.setStudent(student);
            Subject subject = subjectRepository.findById(gradeDto.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException(SUBJECT_NOT_FOUND_MSG + " with id: " + gradeDto.getSubjectId()));
            grade.setSubject(subject);
            gradeRepository.save(grade);
        }
        return mapper.toDto(student);
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
