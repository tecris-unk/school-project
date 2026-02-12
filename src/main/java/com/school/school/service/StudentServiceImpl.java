package com.school.school.service;

import com.school.school.repository.StudentRepository;
import com.school.school.model.Student;
import com.school.school.service.mapper.StudentMapper;
import com.school.school.service.dto.StudentDTO;
import lombok.AllArgsConstructor;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
@Primary
public class StudentServiceImpl implements StudentService {

    private final StudentRepository repository;

    private final StudentMapper mapper;

    @Override
    public List<Student> findAllStudents() {
        return repository.findAll();
    }

    @Override
    public Student createStudent(final StudentDTO dto) {
        Student student = mapper.toEntity(dto);
        repository.save(student);
        return student;
    }

    @Override
    public Student findStudentById(Long id) {
        return repository.findById(id)
                .orElse(null);
    }

    @Override
    public Student findStudentByEmail(String email) {
        return repository.findByEmail(email)
                .orElse(null);
    }

    @Override
    public boolean deleteStudent(Long id) {
        return repository.findById(id)
                .map(user -> {
                    repository.delete(user);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public Student updateStudent(Long id, StudentDTO updatedStudent) {
        Student existingStudent = repository.findById(id)
                .orElse(new Student());
        mapper.updateEntity(existingStudent, updatedStudent);
        repository.save(existingStudent);
        return existingStudent;
    }
}
