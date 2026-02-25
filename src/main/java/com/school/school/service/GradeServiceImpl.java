package com.school.school.service;

import com.school.school.exceptions.ResourceNotFoundException;
import com.school.school.model.Grade;
import com.school.school.model.Student;
import com.school.school.model.Subject;
import com.school.school.repository.GradeRepository;
import com.school.school.repository.StudentRepository;
import com.school.school.repository.SubjectRepository;
import com.school.school.service.dto.GradeDto;
import com.school.school.service.mapper.GradeMapper;
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

    private final GradeRepository repository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final GradeMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<GradeDto> findAllGrades() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GradeDto findGradeById(final Long id) {
        Grade grade = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(GRADE_NOT_FOUND_MSG + " with id: " + id));
        return mapper.toDto(grade);
    }

    @Override
    @Transactional
    public GradeDto createGrade(final GradeDto gradeDto) {
        Student student = studentRepository.findById(gradeDto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        STUDENT_NOT_FOUND_MSG + " with id: " + gradeDto.getStudentId())
                );
        Subject subject = subjectRepository.findById(gradeDto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        SUBJECT_NOT_FOUND_MSG + " with id: " + gradeDto.getSubjectId())
                );

        Grade grade = mapper.toEntity(gradeDto);
        grade.setStudent(student);
        grade.setSubject(subject);
        return mapper.toDto(repository.save(grade));
    }

    @Override
    @Transactional
    public GradeDto updateGrade(final Long id, final GradeDto gradeDto) {
        Student student = studentRepository.findById(gradeDto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        STUDENT_NOT_FOUND_MSG + " with id: " + gradeDto.getStudentId())
                );
        Subject subject = subjectRepository.findById(gradeDto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        SUBJECT_NOT_FOUND_MSG + " with id: " + gradeDto.getSubjectId())
                );

        Grade grade = repository.findById(id)
                .map(existingGrade -> {
                    mapper.updateEntity(existingGrade, gradeDto);
                    existingGrade.setStudent(student);
                    existingGrade.setSubject(subject);
                    repository.save(existingGrade);
                    return existingGrade;
                })
                .orElseThrow(() -> new ResourceNotFoundException(GRADE_NOT_FOUND_MSG + " with id: " + id));
        return mapper.toDto(grade);
    }

    @Override
    @Transactional
    public void deleteGrade(final Long id) {
        Grade grade = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(GRADE_NOT_FOUND_MSG + " with id: " + id));
        repository.delete(grade);
    }
}
