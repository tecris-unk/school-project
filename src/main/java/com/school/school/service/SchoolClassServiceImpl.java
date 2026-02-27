package com.school.school.service;

import com.school.school.controller.mapper.SchoolClassMapper;
import com.school.school.exceptions.ResourceNotFoundException;
import com.school.school.model.SchoolClass;
import com.school.school.model.Subject;
import com.school.school.repository.SchoolClassRepository;
import com.school.school.repository.SubjectRepository;
import com.school.school.service.dto.request.SchoolClassRequest;
import com.school.school.service.dto.response.SchoolClassResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class SchoolClassServiceImpl implements SchoolClassService {

    private static final String SCHOOL_CLASS_NOT_FOUND_MSG = "School class not found";

    private static final String WITH_ID = " with id: ";

    private final SchoolClassRepository repository;
    private final SchoolClassMapper mapper;
    private final SubjectRepository subjectRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SchoolClassResponse> findAllClasses() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolClassResponse> findAllSchoolClassesWithSubjects() {
        return repository.findAllWithSubjectsBy().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolClassResponse findClassById(final Long id) {
        SchoolClass schoolClass = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SCHOOL_CLASS_NOT_FOUND_MSG + WITH_ID + id));
        return mapper.toResponse(schoolClass);
    }

    @Override
    public SchoolClassResponse createClass(final SchoolClassRequest classRequest) {
        SchoolClass schoolClass = repository.save(mapper.toEntity(classRequest));
        return mapper.toResponse(schoolClass);
    }

    @Override
    @Transactional
    public SchoolClassResponse updateClass(final Long id, final SchoolClassRequest classRequest) {
        SchoolClass schoolClass = repository.findById(id)
                .map(existingClass -> {
                    mapper.updateEntity(existingClass, classRequest);
                    return repository.save(existingClass);
                })
                .orElseThrow(() -> new ResourceNotFoundException(SCHOOL_CLASS_NOT_FOUND_MSG  + WITH_ID + id));
        return mapper.toResponse(schoolClass);
    }

    @Override
    @Transactional
    public SchoolClassResponse addSubjectToClass(final Long classId, final Long subjectId) {
        SchoolClass schoolClass = repository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException(SCHOOL_CLASS_NOT_FOUND_MSG + WITH_ID + classId));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found" + WITH_ID + subjectId));

        boolean alreadyAssigned = schoolClass.getSubjects().stream()
                .anyMatch(existingSubject -> existingSubject.getId().equals(subjectId));
        if (!alreadyAssigned) {
            schoolClass.getSubjects().add(subject);
        }

        return mapper.toResponse(repository.save(schoolClass));
    }

    @Override
    @Transactional
    public void deleteClass(final Long id) {
        SchoolClass schoolClass = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SCHOOL_CLASS_NOT_FOUND_MSG + WITH_ID + id));
        repository.delete(schoolClass);
    }
}
