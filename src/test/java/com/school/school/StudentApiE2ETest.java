package com.school.school;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.school.school.model.SchoolClass;
import com.school.school.repository.SchoolClassRepository;
import com.school.school.repository.StudentRepository;
import com.school.school.service.dto.request.StudentRequest;
import com.school.school.service.dto.response.StudentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StudentApiE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SchoolClassRepository schoolClassRepository;

    private Long schoolClassId;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        schoolClassRepository.deleteAll();

        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setGrade(10);
        schoolClass.setLetter("A");
        schoolClassId = schoolClassRepository.save(schoolClass).getId();
    }

    @Test
    void createAndFetchStudent_shouldWorkThroughHttpStack() {
        StudentRequest request = new StudentRequest(
                "Elena",
                "Smirnova",
                "FEMALE",
                "elena.e2e@example.com",
                schoolClassId
        );

        ResponseEntity<StudentResponse> created = restTemplate.postForEntity(
                url("/api/students"), request, StudentResponse.class);

        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertNotNull(created.getBody());
        assertNotNull(created.getBody().getId());
        assertEquals("Elena", created.getBody().getFirstName());
        assertEquals(schoolClassId, created.getBody().getSchoolClassId());

        ResponseEntity<StudentResponse> fetched = restTemplate.exchange(
                url("/api/students/" + created.getBody().getId()),
                HttpMethod.GET,
                HttpEntity.EMPTY,
                StudentResponse.class
        );

        assertEquals(HttpStatus.OK, fetched.getStatusCode());
        assertNotNull(fetched.getBody());
        assertEquals(created.getBody().getId(), fetched.getBody().getId());
        assertEquals("elena.e2e@example.com", fetched.getBody().getEmail());
    }

    private String url(final String path) {
        return "http://localhost:" + port + path;
    }
}