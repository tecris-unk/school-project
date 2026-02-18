package com.school.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.school.model.Student;
import com.school.school.service.StudentService;
import com.school.school.service.dto.StudentDTO;
import com.school.school.service.mapper.StudentMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService service;

    @MockBean
    private StudentMapper mapper;

    @Test
    void findByIdShouldReturnNotFoundWhenStudentMissing() throws Exception {
        when(service.findStudentById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllStudentsShouldReturnNoContentWhenEmpty() throws Exception {
        when(service.findAllStudents()).thenReturn(List.of());

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isNoContent());
    }

    @Test
    void addStudentShouldReturnCreatedDtoBody() throws Exception {
        StudentDTO requestDto = new StudentDTO(null, "Ivan", "Petrov", 10, "MALE", "ivan@mail.com");
        Student saved = new Student(1L, "Ivan", "Petrov", 10, Student.Gender.MALE, "ivan@mail.com", null);
        StudentDTO responseDto = new StudentDTO(1L, "Ivan", "Petrov", 10, "MALE", "ivan@mail.com");

        when(mapper.toDTO(saved)).thenReturn(responseDto);

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("ivan@mail.com"));
    }

    @Test
    void deleteStudentShouldReturnNoContentWhenDeleted() throws Exception {
        when(service.deleteStudent(3L)).thenReturn(true);

        mockMvc.perform(delete("/api/students/3"))
                .andExpect(status().isNoContent());
    }
}