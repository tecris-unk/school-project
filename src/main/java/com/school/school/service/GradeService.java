package com.school.school.service;

import com.school.school.model.Grade;
import com.school.school.service.dto.GradeDto;
import java.util.List;

public interface GradeService {

    List<Grade> findAllGrades();

    Grade findGradeById(Long id);

    void createGrade(GradeDto gradeDto);

    Grade updateGrade(Long id, GradeDto gradeDto);

    void deleteGrade(Long id);
}
