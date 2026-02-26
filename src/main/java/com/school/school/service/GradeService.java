package com.school.school.service;

import com.school.school.service.dto.request.GradeRequest;
import com.school.school.service.dto.response.GradeResponse;
import java.util.List;

public interface GradeService {

    List<GradeResponse> findAllGrades();

    GradeResponse findGradeById(Long id);

    GradeResponse createGrade(GradeRequest gradeRequest);

    GradeResponse updateGrade(Long id, GradeRequest gradeRequest);

    void deleteGrade(Long id);
}
