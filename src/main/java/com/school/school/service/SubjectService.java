package com.school.school.service;

import com.school.school.service.dto.request.SubjectRequest;
import com.school.school.service.dto.response.SubjectResponse;
import java.util.List;

public interface SubjectService {

    List<SubjectResponse> findAllSubjects();

    SubjectResponse findSubjectById(Long id);

    SubjectResponse createSubject(SubjectRequest subjectRequest);

    SubjectResponse updateSubject(Long id, SubjectRequest subjectRequest);

    void deleteSubject(Long id);
}
