package com.school.school.service;

import com.school.school.model.Grade;
import com.school.school.model.Student;
import com.school.school.model.Subject;
import com.school.school.repository.GradeRepository;
import com.school.school.repository.StudentRepository;
import com.school.school.repository.SubjectRepository;
import com.school.school.service.dto.StudentDto;
import com.school.school.service.mapper.StudentMapper;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Primary
public class StudentServiceImpl implements StudentService {

    private final StudentRepository repository;

    private final SubjectRepository subjectRepository;

    private final GradeRepository gradeRepository;

    private final StudentMapper mapper;

    @Override
    public List<Student> findAllStudents() {
        return repository.findAll();
    }

    @Override
    public void createStudent(final StudentDto dto) {
        repository.save(mapper.toEntity(dto));
    }

    @Override
    public Student findStudentById(final Long id) {
        return repository.findById(id)
                .orElse(null);
    }

    @Override
    public Student findStudentByEmail(final String email) {
        return repository.findByEmail(email)
                .orElse(null);
    }

    @Override
    public boolean deleteStudent(final Long id) {
        return repository.findById(id)
                .map(user -> {
                    repository.delete(user);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public Student updateStudent(
            final Long id,
            final StudentDto updatedStudent) {
        return repository.findById(id)
                .map(existingStudent -> {
                    mapper.updateEntity(existingStudent, updatedStudent);
                    repository.save(existingStudent);
                    return existingStudent;
                })
                .orElse(null);
    }

    @Transactional
    @Override
    public void createStudentWithGrades(
            final StudentDto dto,
            final List<Grade> grades) {
        Student student = mapper.toEntity(dto);
        repository.save(student);
        for (Grade grade : grades) {
            grade.setStudent(student);
            Subject subject = subjectRepository.findById(grade.getSubject().getId())
                    .orElseThrow(() -> new RuntimeException("Subject not found"));
            grade.setSubject(subject);
            gradeRepository.save(grade);
        }
    }
}
