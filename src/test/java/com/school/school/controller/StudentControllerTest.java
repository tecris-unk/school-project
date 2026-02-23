package com.school.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.school.model.Student;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    void findByIdShouldReturnStudentWhenFound() throws Exception {
        Student student = new Student();
        StudentDTO dto = new StudentDTO(1L, "Ivan", "Petrov", 10, "MALE", "ivan@mail.com");
        when(service.findStudentById(1L)).thenReturn(student);
        when(mapper.toDTO(student)).thenReturn(dto);

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@mail.com"));
    }

    @Test
    void findByEmailShouldReturnNotFoundWhenStudentMissing() throws Exception {
        when(service.findStudentByEmail("missing@mail.com")).thenReturn(null);

        mockMvc.perform(get("/api/students/by-email").param("email", "missing@mail.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findByEmailShouldReturnStudentWhenFound() throws Exception {
        Student student = new Student();
        StudentDTO dto = new StudentDTO(2L, "Anna", "Smirnova", 9, "FEMALE", "anna@mail.com");
        when(service.findStudentByEmail("anna@mail.com")).thenReturn(student);
        when(mapper.toDTO(student)).thenReturn(dto);

        mockMvc.perform(get("/api/students/by-email").param("email", "anna@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.firstName").value("Anna"));
    }

    @Test
    void getAllStudentsWithSubjectsShouldReturnNoContentWhenEmpty() throws Exception {
        when(service.findAllStudentsWithSubjects()).thenReturn(List.of());

        mockMvc.perform(get("/api/students/with-subjects"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAllStudentsWithSubjectsShouldReturnMappedStudentsWhenPresent() throws Exception {
        Student first = new Student();
        first.setId(1L);
        StudentDTO firstDto = new StudentDTO(1L, "Ivan", "Petrov", 10, "MALE", "ivan@mail.com");

        when(service.findAllStudentsWithSubjects()).thenReturn(List.of(first));
        when(mapper.toDTO(first)).thenReturn(firstDto);

        mockMvc.perform(get("/api/students/with-subjects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Ivan"));
    }

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
    void getAllStudentsShouldReturnMappedStudentsWhenPresent() throws Exception {
        Student first = new Student();
        first.setId(1L);
        Student second = new Student();
        second.setId(2L);
        StudentDTO firstDto = new StudentDTO(1L, "Ivan", "Petrov", 10, "MALE", "ivan@mail.com");
        StudentDTO secondDto = new StudentDTO(2L, "Anna", "Smirnova", 9, "FEMALE", "anna@mail.com");

        when(service.findAllStudents()).thenReturn(List.of(first, second));
        when(mapper.toDTO(first)).thenReturn(firstDto);
        when(mapper.toDTO(second)).thenReturn(secondDto);

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Ivan"))
                .andExpect(jsonPath("$[1].firstName").value("Anna"));
    }

    @Test
    void deleteStudentShouldReturnNoContentWhenDeleted() throws Exception {
        when(service.deleteStudent(3L)).thenReturn(true);

        mockMvc.perform(delete("/api/students/3"))
                .andExpect(status().isNoContent());
    }
    @Test
    void updateStudentShouldReturnMappedStudent() throws Exception {
        Student updated = new Student();
        StudentDTO requestDto = new StudentDTO(null, "Ivan", "Updated", 11, "MALE", "ivan@mail.com");
        StudentDTO responseDto = new StudentDTO(1L, "Ivan", "Updated", 11, "MALE", "ivan@mail.com");

        when(service.updateStudent(eq(1L), any(StudentDTO.class))).thenReturn(updated);
        when(mapper.toDTO(updated)).thenReturn(responseDto);

        mockMvc.perform(put("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Updated"));
    }

    @Test
    void updateStudentShouldReturnNotFoundWhenServiceReturnsNull() throws Exception {
        StudentDTO requestDto = new StudentDTO(null, "Ivan", "Updated", 11, "MALE", "ivan@mail.com");
        when(service.updateStudent(eq(1L), any(StudentDTO.class))).thenReturn(null);

        mockMvc.perform(put("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }
    @Test
    void addStudentShouldReturnBadRequestForInvalidPayload() throws Exception {
        StudentDTO invalidDto = new StudentDTO(null, "", "Petrov", 20, "", "not-an-email");

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(service, never()).createStudent(any(StudentDTO.class));
    }
}
