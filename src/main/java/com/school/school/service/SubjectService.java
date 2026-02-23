package com.school.school.service;

import com.school.school.model.Subject;
import com.school.school.service.dto.SubjectDto;
import java.util.List;

public interface SubjectService {

    List<Subject> findAllSubjects();

    Subject findSubjectById(Long id);

    Subject createSubject(SubjectDto subjectDto);

    Subject updateSubject(Long id, SubjectDto subjectDto);

    boolean deleteSubject(Long id);
}
