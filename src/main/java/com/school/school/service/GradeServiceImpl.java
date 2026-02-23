package com.school.school.service;

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

@Service
@AllArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeRepository repository;

    private final StudentRepository studentRepository;

    private final SubjectRepository subjectRepository;

    private final GradeMapper mapper;

    @Override
    public List<Grade> findAllGrades() {
        return repository.findAll();
    }

    @Override
    public Grade findGradeById(final Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Grade createGrade(final GradeDto gradeDto) {
        Student student = studentRepository.findById(gradeDto.getStudentId()).orElse(null);
        Subject subject = subjectRepository.findById(gradeDto.getSubjectId()).orElse(null);
        if (student == null || subject == null) {
            return null;
        }

        Grade grade = mapper.toEntity(gradeDto);
        grade.setStudent(student);
        grade.setSubject(subject);
        repository.save(grade);
        return grade;
    }

    @Override
    public Grade updateGrade(final Long id, final GradeDto gradeDto) {
        Student student = studentRepository.findById(gradeDto.getStudentId()).orElse(null);
        Subject subject = subjectRepository.findById(gradeDto.getSubjectId()).orElse(null);
        if (student == null || subject == null) {
            return null;
        }

        return repository.findById(id)
                .map(existingGrade -> {
                    mapper.updateEntity(existingGrade, gradeDto);
                    existingGrade.setStudent(student);
                    existingGrade.setSubject(subject);
                    repository.save(existingGrade);
                    return existingGrade;
                })
                .orElse(null);
    }

    @Override
    public boolean deleteGrade(final Long id) {
        return repository.findById(id)
                .map(grade -> {
                    repository.delete(grade);
                    return true;
                })
                .orElse(false);
    }
}
