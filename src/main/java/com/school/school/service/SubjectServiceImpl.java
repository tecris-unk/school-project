package com.school.school.service;

import com.school.school.exceptions.ResourceNotFoundException;
import com.school.school.model.Subject;
import com.school.school.model.Teacher;
import com.school.school.repository.SubjectRepository;
import com.school.school.repository.TeacherRepository;
import com.school.school.service.dto.request.SubjectRequest;
import com.school.school.controller.mapper.SubjectMapper;
import com.school.school.service.dto.response.SubjectResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private static final String SUBJECT_NOT_FOUND_MSG = "Subject not found";
    private static final String TEACHER_NOT_FOUND_MSG = "Teacher not found";

    private final SubjectRepository repository;
    private final TeacherRepository teacherRepository;
    private final SubjectMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> findAllSubjects() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public SubjectResponse findSubjectById(final Long id) {
        Subject subject = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SUBJECT_NOT_FOUND_MSG + " with id: " + id));
        return mapper.toResponse(subject);
    }

    @Override
    @Transactional
    public SubjectResponse createSubject(final SubjectRequest subjectRequest) {
        Subject subject = mapper.toEntity(subjectRequest);
        if (subjectRequest.getTeacherId() != null) {
            Teacher teacher = teacherRepository
                    .findById(subjectRequest.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            TEACHER_NOT_FOUND_MSG + " with id: " + subjectRequest.getTeacherId())
                    );
            subject.setTeacher(teacher);
        }
        return mapper.toResponse(repository.save(subject));
    }

    @Override
    @Transactional
    public SubjectResponse updateSubject(final Long id, final SubjectRequest subjectRequest) {
        Subject subject = repository.findById(id)
                .map(existingSubject -> {
                    mapper.updateEntity(existingSubject, subjectRequest);
                    if (subjectRequest.getTeacherId() == null) {
                        existingSubject.setTeacher(null);
                    } else {
                        Teacher teacher = teacherRepository
                                .findById(subjectRequest.getTeacherId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        TEACHER_NOT_FOUND_MSG + " with id: " + subjectRequest.getTeacherId())
                                );
                        existingSubject.setTeacher(teacher);
                    }
                    return repository.save(existingSubject);
                })
                .orElseThrow(() -> new ResourceNotFoundException(SUBJECT_NOT_FOUND_MSG + " with id: " + id));
        return mapper.toResponse(subject);
    }

    @Override
    @Transactional
    public void deleteSubject(final Long id) {
        Subject subject = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SUBJECT_NOT_FOUND_MSG + " with id: " + id));
        repository.delete(subject);
    }
}
