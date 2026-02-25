package com.school.school.service;

import com.school.school.service.dto.GradeDto;
import java.util.List;

public interface GradeService {

    List<GradeDto> findAllGrades();

    GradeDto findGradeById(Long id);

    GradeDto createGrade(GradeDto gradeDto);

    GradeDto updateGrade(Long id, GradeDto gradeDto);

    void deleteGrade(Long id);
}
