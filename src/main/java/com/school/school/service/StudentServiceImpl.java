package com.school.school.service;

import com.school.school.controller.mapper.GradeMapper;
import com.school.school.controller.mapper.StudentMapper;
import com.school.school.exceptions.ConflictException;
import com.school.school.exceptions.ResourceNotFoundException;
import com.school.school.exceptions.ValidationException;
import com.school.school.model.Grade;
import com.school.school.model.SchoolClass;
import com.school.school.model.Student;
import com.school.school.model.Subject;
import com.school.school.repository.GradeRepository;
import com.school.school.repository.SchoolClassRepository;
import com.school.school.repository.StudentRepository;
import com.school.school.repository.SubjectRepository;
import com.school.school.service.dto.request.GradeRequest;
import com.school.school.service.dto.request.StudentRequest;
import com.school.school.service.dto.request.StudentWithGradesRequest;
import com.school.school.service.dto.response.StudentResponse;
import jakarta.annotation.Nullable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Primary
public class StudentServiceImpl implements StudentService {

    private static final String STUDENT_NOT_FOUND_MSG = "Student not found";
    private static final String SUBJECT_NOT_FOUND_MSG = "Subject not found";
    private static final String SCHOOL_CLASS_NOT_FOUND_MSG = "School class not found";
    private static final String EMAIL_ALREADY_EXISTS_MSG = "Student with this email already exists";
    private static final String EMAIL_REQUIRED_MSG = "Email must not be blank";

    private static final String WITH_ID = " with id: ";

    private final StudentRepository repository;
    private final SubjectRepository subjectRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final GradeRepository gradeRepository;
    private final StudentMapper mapper;
    private final GradeMapper gradeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> findAllStudentsWithGrades() {
        return repository.findAllWithGradesBy().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public StudentResponse createStudent(final StudentRequest request) {
        validateEmail(request.getEmail());
        ensureEmailUnique(request.getEmail());
        Student student = mapper.toEntity(request);
        applySchoolClass(student, request.getSchoolClassId());
        Student saved = repository.save(student);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse findStudentById(final Long id) {
        Student student = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(STUDENT_NOT_FOUND_MSG + WITH_ID + id));
        return mapper.toResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> findStudentsByEmailAndDate(
            @Nullable final String email,
            @Nullable final LocalDate date,
            final Pageable pageable
    ) {
        Page<Student> studentPage = repository.findStudentsByEmail(email, pageable);
        List<Student> students = studentPage.getContent();

        if (date != null && !students.isEmpty()) {
            List<Grade> grades = gradeRepository.findGradesByStudentsAndDate(students, date);

            Map<Long, List<Grade>> gradesByStudentId = grades.stream()
                    .collect(Collectors.groupingBy(g -> g.getStudent().getId()));

            students.forEach(student -> {
                List<Grade> studentGrades = gradesByStudentId.getOrDefault(student.getId(), Collections.emptyList());
                student.setGrades(studentGrades);
            });
        }

        return studentPage.map(mapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteStudent(final Long id) {
        Student student = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(STUDENT_NOT_FOUND_MSG + WITH_ID + id));
        repository.delete(student);
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(final Long id, final StudentRequest updatedStudent) {
        validateEmail(updatedStudent.getEmail());
        Student student = repository.findById(id)
                .map(existingStudent -> {
                    ensureEmailUniqueForUpdate(updatedStudent.getEmail(), existingStudent.getId());
                    mapper.updateEntity(existingStudent, updatedStudent);
                    applySchoolClass(existingStudent, updatedStudent.getSchoolClassId());
                    return repository.save(existingStudent);
                })
                .orElseThrow(() -> new ResourceNotFoundException(STUDENT_NOT_FOUND_MSG + WITH_ID + id));
        return mapper.toResponse(student);
    }

    @Override
    @Transactional
    public StudentResponse createStudentWithGradesTransactional(final StudentWithGradesRequest request) {
        StudentRequest studentRequest = request.getStudent();
        validateEmail(studentRequest.getEmail());
        ensureEmailUnique(studentRequest.getEmail());

        Student student = mapper.toEntity(studentRequest);
        applySchoolClass(student, studentRequest.getSchoolClassId());
        student = repository.save(student);

        for (GradeRequest gradeRequest : request.getGrades()) {
            Grade grade = gradeMapper.toEntity(gradeRequest);
            grade.setStudent(student);
            Subject subject = subjectRepository.findById(gradeRequest.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            SUBJECT_NOT_FOUND_MSG + WITH_ID + gradeRequest.getSubjectId())
                    );
            grade.setSubject(subject);
            gradeRepository.save(grade);
        }
        return mapper.toResponse(student);
    }

    @Override
    public StudentResponse createStudentWithGradesNoTransactional(final StudentWithGradesRequest request) {
        StudentRequest studentRequest = request.getStudent();
        validateEmail(studentRequest.getEmail());
        ensureEmailUnique(studentRequest.getEmail());

        Student student = mapper.toEntity(studentRequest);
        applySchoolClass(student, studentRequest.getSchoolClassId());
        student = repository.save(student);

        for (GradeRequest gradeRequest : request.getGrades()) {
            Grade grade = gradeMapper.toEntity(gradeRequest);
            grade.setStudent(student);
            Subject subject = subjectRepository.findById(gradeRequest.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            SUBJECT_NOT_FOUND_MSG + WITH_ID + gradeRequest.getSubjectId())
                    );
            grade.setSubject(subject);
            gradeRepository.save(grade);
        }
        return mapper.toResponse(student);
    }

    private void applySchoolClass(final Student student, final Long schoolClassId) {
        if (schoolClassId == null) {
            student.setSchoolClass(null);
            return;
        }

        SchoolClass schoolClass = schoolClassRepository.findById(schoolClassId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        SCHOOL_CLASS_NOT_FOUND_MSG + WITH_ID + schoolClassId)
                );
        student.setSchoolClass(schoolClass);
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
