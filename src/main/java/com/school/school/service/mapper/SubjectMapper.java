package com.school.school.service.mapper;

import com.school.school.model.Grade;
import com.school.school.model.SchoolClass;
import com.school.school.model.Subject;
import com.school.school.model.Teacher;
import com.school.school.service.dto.GradeDto;
import com.school.school.service.dto.SchoolClassDto;
import com.school.school.service.dto.SubjectDto;
import com.school.school.service.dto.TeacherDto;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public final class SubjectMapper {

    public SubjectDto toDto(final Subject subject) {
        if (subject == null) {
            return null;
        }

        SubjectDto dto = new SubjectDto();
        dto.setId(subject.getId());
        dto.setName(subject.getName());
        dto.setDescription(subject.getDescription());
        dto.setTeacher(toTeacherSummary(subject.getTeacher()));
        dto.setGrades(toGradeSummaries(subject.getGrades()));
        dto.setSchoolClasses(toSchoolClassSummaries(subject.getSchoolClasses()));
        return dto;
    }

    public Subject toEntity(final SubjectDto dto) {
        if (dto == null) {
            return null;
        }

        Subject subject = new Subject();
        subject.setName(dto.getName());
        subject.setDescription(dto.getDescription());
        return subject;
    }

    public void updateEntity(final Subject subject, final SubjectDto dto) {
        subject.setName(dto.getName());
        subject.setDescription(dto.getDescription());
    }

    private TeacherDto toTeacherSummary(final Teacher teacher) {
        if (teacher == null) {
            return null;
        }

        TeacherDto dto = new TeacherDto();
        dto.setId(teacher.getId());
        dto.setFirstName(teacher.getFirstName());
        dto.setLastName(teacher.getLastName());
        dto.setEmail(teacher.getEmail());
        return dto;
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

    private List<SchoolClassDto> toSchoolClassSummaries(final List<SchoolClass> schoolClasses) {
        if (schoolClasses == null) {
            return new ArrayList<>();
        }

        return schoolClasses.stream().map(schoolClass -> {
            SchoolClassDto dto = new SchoolClassDto();
            dto.setId(schoolClass.getId());
            dto.setGrade(schoolClass.getGrade());
            dto.setLetter(schoolClass.getLetter());
            return dto;
        }).toList();
    }
}
