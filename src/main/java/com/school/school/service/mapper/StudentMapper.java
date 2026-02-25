package com.school.school.service.mapper;

import com.school.school.model.Grade;
import com.school.school.model.SchoolClass;
import com.school.school.model.Student;
import com.school.school.service.dto.GradeDto;
import com.school.school.service.dto.SchoolClassDto;
import com.school.school.service.dto.StudentDto;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public final class StudentMapper {

    @NotNull
    public StudentDto toDto(final Student student) {
        if (student == null) {
            return null;
        }

        StudentDto dto = new StudentDto();
        dto.setId(student.getId());
        dto.setFirstName(student.getFirstName());
        dto.setLastName(student.getLastName());
        dto.setGender(student.getGender() != null ? student.getGender().name() : null);
        dto.setEmail(student.getEmail());
        dto.setSchoolClass(toSchoolClassSummary(student.getSchoolClass()));
        dto.setGrades(toGradeSummaries(student.getGrades()));
        return dto;
    }

    public Student toEntity(final StudentDto dto) {
        if (dto == null) {
            return null;
        }

        Student student = new Student();
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setGender(parseGender(dto.getGender()));
        student.setEmail(dto.getEmail());

        return student;
    }

    public void updateEntity(final Student student, final StudentDto dto) {
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setGender(parseGender(dto.getGender()));
        student.setEmail(dto.getEmail());
    }

    private List<GradeDto> toGradeSummaries(final List<Grade> grades) {
        if (grades == null) {
            return new ArrayList<>();
        }

        return grades.stream().map(grade -> {
            GradeDto dto = new GradeDto();
            dto.setId(grade.getId());
            dto.setScore(grade.getScore());
            dto.setDate(grade.getDate());
            return dto;
        }).toList();
    }

    private SchoolClassDto toSchoolClassSummary(final SchoolClass schoolClass) {
        if (schoolClass == null) {
            return null;
        }

        SchoolClassDto dto = new SchoolClassDto();
        dto.setId(schoolClass.getId());
        dto.setGrade(schoolClass.getGrade());
        dto.setLetter(schoolClass.getLetter());
        return dto;
    }

    private Student.Gender parseGender(final String gender) {
        if (gender == null) {
            return null;
        }
        return Student.Gender.valueOf(gender.trim().toUpperCase(Locale.ROOT));
    }
}
