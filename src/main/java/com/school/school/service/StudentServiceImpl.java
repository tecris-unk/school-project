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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    private final Map<StudentSearchCacheKey, Page<StudentResponse>> searchCache = new HashMap<>();

    @Override
    public StudentResponse createStudent(final StudentRequest request) {
        validateEmail(request.getEmail());
        ensureEmailUnique(request.getEmail());
        Student student = mapper.toEntity(request);
        applySchoolClass(student, request.getSchoolClassId());
        Student saved = repository.save(student);
        invalidateSearchCache();
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
    public Page<StudentResponse> findStudentsByNestedFilters(
            final String teacherEmail,
            final String subjectName,
            final Integer minScore,
            final Pageable pageable,
            final StudentSearchQueryType queryType
    ) {
        String normalizedSubjectName = subjectName == null || subjectName.isBlank()
                ? null
                : subjectName.trim().toLowerCase();

        StudentSearchCacheKey cacheKey = StudentSearchCacheKey.of(
                teacherEmail,
                normalizedSubjectName,
                minScore,
                pageable,
                queryType
        );
        Page<StudentResponse> cachedResult = searchCache.get(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        Page<Long> studentIdsPage = queryType == StudentSearchQueryType.NATIVE
                ? repository.findStudentIdsByNestedFiltersNative(teacherEmail, normalizedSubjectName, minScore, pageable)
                : repository.findStudentIdsByNestedFiltersJpql(
                teacherEmail,
                normalizedSubjectName,
                minScore,
                pageable
        );


        List<Long> ids = studentIdsPage.getContent();
        if (ids.isEmpty()) {
            Page<StudentResponse> emptyPage = new PageImpl<>(List.of(), pageable, studentIdsPage.getTotalElements());
            searchCache.put(cacheKey, emptyPage);
            return emptyPage;
        }

        List<Student> students = repository.findAllByIdsWithGradesAndTeacher(ids);
        Map<Long, Student> studentsById = students.stream()
                .collect(Collectors.toMap(Student::getId, student -> student, (left, right) -> left, LinkedHashMap::new));

        List<StudentResponse> orderedResponses = ids.stream()
                .map(studentsById::get)
                .filter(Objects::nonNull)
                .map(mapper::toResponse)
                .toList();

        Page<StudentResponse> mappedPage = new PageImpl<>(orderedResponses, pageable, studentIdsPage.getTotalElements());
        searchCache.put(cacheKey, mappedPage);
        return mappedPage;
    }

    @Override
    @Transactional
    public void deleteStudent(final Long id) {
        Student student = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(STUDENT_NOT_FOUND_MSG + WITH_ID + id));
        repository.delete(student);
        invalidateSearchCache();
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
        invalidateSearchCache();
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
        invalidateSearchCache();
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
        invalidateSearchCache();
        return mapper.toResponse(student);
    }

    private void invalidateSearchCache() {
        searchCache.clear();
    }

    @AllArgsConstructor
    private static final class StudentSearchCacheKey {
        private final String teacherEmail;
        private final String subjectName;
        private final Integer minScore;
        private final int pageNumber;
        private final int pageSize;
        private final String sort;
        private final StudentSearchQueryType queryType;

        private static StudentSearchCacheKey of(
                final String teacherEmail,
                final String subjectName,
                final Integer minScore,
                final Pageable pageable,
                final StudentSearchQueryType queryType
        ) {
            return new StudentSearchCacheKey(
                    teacherEmail,
                    subjectName,
                    minScore,
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    pageable.getSort().toString(),
                    queryType
            );
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            StudentSearchCacheKey that = (StudentSearchCacheKey) o;
            return pageNumber == that.pageNumber
                    && pageSize == that.pageSize
                    && Objects.equals(teacherEmail, that.teacherEmail)
                    && Objects.equals(subjectName, that.subjectName)
                    && Objects.equals(minScore, that.minScore)
                    && Objects.equals(sort, that.sort)
                    && queryType == that.queryType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(teacherEmail, subjectName, minScore, pageNumber, pageSize, sort, queryType);
        }
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
