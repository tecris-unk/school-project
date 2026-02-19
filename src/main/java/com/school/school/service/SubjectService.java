package com.school.school.service;

import com.school.school.model.Subject;
import com.school.school.service.dto.SubjectDTO;
import java.util.List;

public interface SubjectService {

    List<Subject> findAllSubjects();

    Subject findSubjectById(Long id);

    Subject createSubject(SubjectDTO subjectDTO);

    Subject updateSubject(Long id, SubjectDTO subjectDTO);

    boolean deleteSubject(Long id);
}
