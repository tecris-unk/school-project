package com.school.school.service;

import com.school.school.model.Grade;
import com.school.school.service.dto.GradeDTO;
import java.util.List;

public interface GradeService {

    List<Grade> findAllGrades();

    Grade findGradeById(Long id);

    Grade createGrade(GradeDTO gradeDTO);

    Grade updateGrade(Long id, GradeDTO gradeDTO);

    boolean deleteGrade(Long id);
}
