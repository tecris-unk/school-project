package com.school.school.service;

import com.school.school.service.dto.SubjectDto;
import java.util.List;

public interface SubjectService {

    List<SubjectDto> findAllSubjects();

    SubjectDto findSubjectById(Long id);

    SubjectDto createSubject(SubjectDto subjectDto);

    SubjectDto updateSubject(Long id, SubjectDto subjectDto);

    void deleteSubject(Long id);
}
