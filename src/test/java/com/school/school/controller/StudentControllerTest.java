package com.school.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.school.model.Student;
import com.school.school.service.StudentService;
import com.school.school.service.dto.StudentDto;
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
        StudentDto dto = new StudentDto(1L, "Ivan", "Petrov", 10, "MALE", "ivan@mail.com");
        when(service.findStudentById(1L)).thenReturn(student);
        when(mapper.toDto(student)).thenReturn(dto);

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
        StudentDto dto = new StudentDto(2L, "Anna", "Smirnova", 9, "FEMALE", "anna@mail.com");
        when(service.findStudentByEmail("anna@mail.com")).thenReturn(student);
        when(mapper.toDto(student)).thenReturn(dto);

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
        StudentDto firstDto = new StudentDto(1L, "Ivan", "Petrov", 10, "MALE", "ivan@mail.com");

        when(service.findAllStudentsWithSubjects()).thenReturn(List.of(first));
        when(mapper.toDto(first)).thenReturn(firstDto);

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
        StudentDto requestDto = new StudentDto(null, "Ivan", "Petrov", 10, "MALE", "ivan@mail.com");

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        ArgumentCaptor<StudentDto> captor = ArgumentCaptor.forClass(StudentDto.class);
        verify(service).createStudent(captor.capture());

        StudentDto captured = captor.getValue();
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
        StudentDto firstDto = new StudentDto(1L, "Ivan", "Petrov", 10, "MALE", "ivan@mail.com");
        StudentDto secondDto = new StudentDto(2L, "Anna", "Smirnova", 9, "FEMALE", "anna@mail.com");

        when(service.findAllStudents()).thenReturn(List.of(first, second));
        when(mapper.toDto(first)).thenReturn(firstDto);
        when(mapper.toDto(second)).thenReturn(secondDto);

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
        StudentDto requestDto = new StudentDto(null, "Ivan", "Updated", 11, "MALE", "ivan@mail.com");
        StudentDto responseDto = new StudentDto(1L, "Ivan", "Updated", 11, "MALE", "ivan@mail.com");

        when(service.updateStudent(eq(1L), any(StudentDto.class))).thenReturn(updated);
        when(mapper.toDto(updated)).thenReturn(responseDto);

        mockMvc.perform(put("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Updated"));
    }

    @Test
    void updateStudentShouldReturnNotFoundWhenServiceReturnsNull() throws Exception {
        StudentDto requestDto = new StudentDto(null, "Ivan", "Updated", 11, "MALE", "ivan@mail.com");
        when(service.updateStudent(eq(1L), any(StudentDto.class))).thenReturn(null);

        mockMvc.perform(put("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }
    @Test
    void addStudentShouldReturnBadRequestForInvalidPayload() throws Exception {
        StudentDto invalidDto = new StudentDto(null, "", "Petrov", 20, "", "not-an-email");

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(service, never()).createStudent(any(StudentDto.class));
    }
}
