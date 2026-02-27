package com.school.school.service;

import com.school.school.service.dto.request.SchoolClassRequest;
import com.school.school.service.dto.response.SchoolClassResponse;
import java.util.List;

public interface SchoolClassService {

    List<SchoolClassResponse> findAllClasses();

    List<SchoolClassResponse> findAllSchoolClassesWithSubjects();

    SchoolClassResponse findClassById(Long id);

    SchoolClassResponse createClass(SchoolClassRequest classRequest);

    SchoolClassResponse updateClass(Long id, SchoolClassRequest classRequest);

    SchoolClassResponse addSubjectToClass(Long classId, Long subjectId);

    void deleteClass(Long id);
}
