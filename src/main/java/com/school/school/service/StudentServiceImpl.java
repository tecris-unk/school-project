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

    private final StudentRepository studentRepository;

    private final StudentMapper studentMapper;
    @Override
    public List<Student> findAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public Student createStudent(final StudentDTO studentDTO) {
        Student student = studentMapper.toEntity(studentDTO);
        studentRepository.save(student);
        return student;
    }

    @Override
    public Student findStudentById(Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    @Override
    public Student updateStudent(Long id, StudentDTO updatedStudent) {
        Student existingStudent = studentRepository.findById(id)
                .orElse(new Student());
        studentMapper.updateEntity(existingStudent, updatedStudent);
        studentRepository.save(existingStudent);
        return existingStudent;
    }
}
