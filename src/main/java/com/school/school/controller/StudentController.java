package com.school.school.controller;

import com.school.school.model.Student;
import com.school.school.service.StudentService;
import com.school.school.service.dto.StudentDTO;
import com.school.school.service.mapper.StudentMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@AllArgsConstructor
public class StudentController {

    private final StudentService studentService;

    private final StudentMapper studentMapper;

    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> findById(@PathVariable Long id) {
        Student student = studentService.findStudentById(id);
        if (student == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(studentMapper.toDTO(student));
    }

    @GetMapping()
    public List<StudentDTO> getAllStudents() {
        List<Student> allStudents = studentService.findAllStudents();
        return allStudents.stream()
                .map(studentMapper::toDTO)
                .toList();
    }

    @GetMapping()

    @PostMapping
    public StudentDTO addStudent(@RequestBody StudentDTO studentDTO) {
        Student student = studentService.createStudent(studentDTO);
        return studentMapper.toDTO(student);
    }

    @PutMapping("/{id}")
    public StudentDTO updateStudent(@PathVariable Long id, @RequestBody StudentDTO updatedStudent) {
        Student student = studentService.updateStudent(id, updatedStudent);
        return studentMapper.toDTO(student);
    }

    @DeleteMapping("/{id}")
    public void deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
    }


}
