package com.school.school.service.mapper;

import com.school.school.model.SchoolClass;
import com.school.school.model.Student;
import com.school.school.model.Subject;
import com.school.school.service.dto.SchoolClassDto;
import com.school.school.service.dto.StudentDto;
import com.school.school.service.dto.SubjectDto;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public final class SchoolClassMapper {

    public SchoolClassDto toDto(final SchoolClass schoolClass) {
        if (schoolClass == null) {
            return null;
        }

        SchoolClassDto dto = new SchoolClassDto();
        dto.setId(schoolClass.getId());
        dto.setGrade(schoolClass.getGrade());
        dto.setLetter(schoolClass.getLetter());
        dto.setStudents(toStudentSummaries(schoolClass.getStudents()));
        dto.setSubjects(toSubjectSummaries(schoolClass.getSubjects()));
        return dto;
    }

    public SchoolClass toEntity(final SchoolClassDto dto) {
        if (dto == null) {
            return null;
        }

        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setGrade(dto.getGrade());
        schoolClass.setLetter(dto.getLetter());
        return schoolClass;
    }

    public void updateEntity(
            final SchoolClass schoolClass,
            final SchoolClassDto dto) {
        schoolClass.setGrade(dto.getGrade());
        schoolClass.setLetter(dto.getLetter());
    }

    private List<StudentDto> toStudentSummaries(final List<Student> students) {
        if (students == null) {
            return new ArrayList<>();
        }

        return students.stream().map(student -> {
            StudentDto dto = new StudentDto();
            dto.setId(student.getId());
            dto.setFirstName(student.getFirstName());
            dto.setLastName(student.getLastName());
            dto.setGender(student.getGender() != null ? student.getGender().name() : null);
            dto.setEmail(student.getEmail());
            return dto;
        }).toList();
    }

    private List<SubjectDto> toSubjectSummaries(final List<Subject> subjects) {
        if (subjects == null) {
            return new ArrayList<>();
        }

        return subjects.stream().map(subject -> {
            SubjectDto dto = new SubjectDto();
            dto.setId(subject.getId());
            dto.setName(subject.getName());
            dto.setDescription(subject.getDescription());
            return dto;
        }).toList();
    }
}
