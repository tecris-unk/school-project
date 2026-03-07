package com.school.school.service;

import com.school.school.controller.mapper.GradeMapper;
import com.school.school.exceptions.ResourceNotFoundException;
import com.school.school.model.Grade;
import com.school.school.model.Student;
import com.school.school.model.Subject;
import com.school.school.repository.GradeRepository;
import com.school.school.repository.StudentRepository;
import com.school.school.repository.SubjectRepository;
import com.school.school.service.dto.request.GradeRequest;
import com.school.school.service.dto.response.GradeResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class GradeServiceImpl implements GradeService {

    private static final String GRADE_NOT_FOUND_MSG = "Grade not found";
    private static final String STUDENT_NOT_FOUND_MSG = "Student not found";
    private static final String SUBJECT_NOT_FOUND_MSG = "Subject not found";

    private static final String WITH_ID = " with id: ";

    private final GradeRepository repository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final GradeMapper mapper;
    private final StudentSearchCacheIndex searchCacheIndex;

    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> findAllGrades() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GradeResponse findGradeById(final Long id) {
        Grade grade = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(GRADE_NOT_FOUND_MSG + WITH_ID + id));
        return mapper.toResponse(grade);
    }

    @Override
    @Transactional
    public GradeResponse createGrade(final GradeRequest gradeRequest) {
        Student student = studentRepository.findById(gradeRequest.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        STUDENT_NOT_FOUND_MSG + WITH_ID + gradeRequest.getStudentId())
                );
        Subject subject = subjectRepository.findById(gradeRequest.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        SUBJECT_NOT_FOUND_MSG + WITH_ID + gradeRequest.getSubjectId())
                );

        Grade grade = mapper.toEntity(gradeRequest);
        grade.setStudent(student);
        grade.setSubject(subject);
        GradeResponse savedGrade = mapper.toResponse(repository.save(grade));
        searchCacheIndex.clear();
        return savedGrade;
    }

    @Override
    @Transactional
    public GradeResponse updateGrade(final Long id, final GradeRequest gradeRequest) {
        Student student = studentRepository.findById(gradeRequest.getStudentId())

                .orElseThrow(() -> new ResourceNotFoundException(
                        STUDENT_NOT_FOUND_MSG + WITH_ID + gradeRequest.getStudentId())
                );
        Subject subject = subjectRepository.findById(gradeRequest.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        SUBJECT_NOT_FOUND_MSG + WITH_ID + gradeRequest.getSubjectId())
                );

        Grade grade = repository.findById(id)
                .map(existingGrade -> {
                    mapper.updateEntity(existingGrade, gradeRequest);
                    existingGrade.setStudent(student);
                    existingGrade.setSubject(subject);
                    repository.save(existingGrade);
                    return existingGrade;
                })
                .orElseThrow(() -> new ResourceNotFoundException(GRADE_NOT_FOUND_MSG + WITH_ID + id));
        searchCacheIndex.clear();
        return mapper.toResponse(grade);
    }

    @Override
    @Transactional
    public void deleteGrade(final Long id) {
        Grade grade = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(GRADE_NOT_FOUND_MSG + WITH_ID + id));
        searchCacheIndex.clear();
        repository.delete(grade);
    }
}
