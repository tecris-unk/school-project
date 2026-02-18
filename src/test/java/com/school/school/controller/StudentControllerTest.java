package com.school.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.school.service.StudentService;
import com.school.school.service.dto.StudentDTO;
import com.school.school.service.mapper.StudentMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        ArgumentCaptor<StudentDTO> captor = ArgumentCaptor.forClass(StudentDTO.class);
        verify(service).createStudent(captor.capture());

        StudentDTO captured = captor.getValue();
        assertAll(
                () -> assertNull(captured.getId()),
                () -> assertEquals("Ivan", captured.getFirstName()),
                () -> assertEquals("Petrov", captured.getLastName()),
                () -> assertEquals(10, captured.getGrade()),
                () -> assertEquals("MALE", captured.getGender()),
                () -> assertEquals("ivan@mail.com", captured.getEmail())
        );
    }

    @Test
    void deleteStudentShouldReturnNoContentWhenDeleted() throws Exception {
        when(service.deleteStudent(3L)).thenReturn(true);

        mockMvc.perform(delete("/api/students/3"))
                .andExpect(status().isNoContent());
    }
}