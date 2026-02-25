package com.school.school.service;

import com.school.school.exceptions.ResourceNotFoundException;
import com.school.school.model.SchoolClass;
import com.school.school.repository.SchoolClassRepository;
import com.school.school.service.dto.SchoolClassDto;
import com.school.school.service.mapper.SchoolClassMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class SchoolClassServiceImpl implements SchoolClassService {

    private static final String SCHOOL_CLASS_NOT_FOUND_MSG = "School class not found";

    private final SchoolClassRepository repository;
    private final SchoolClassMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<SchoolClass> findAllClasses() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolClass> findAllSchoolClassesWithSubjects() {
        return repository.findAllWithSubjectsBy();
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolClass findClassById(final Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SCHOOL_CLASS_NOT_FOUND_MSG + " with id: " + id));
    }

    @Override
    public void createClass(final SchoolClassDto classDto) {
        repository.save(mapper.toEntity(classDto));
    }

    @Override
    @Transactional
    public SchoolClass updateClass(final Long id, final SchoolClassDto classDto) {
        return repository.findById(id)
                .map(existingClass -> {
                    mapper.updateEntity(existingClass, classDto);
                    repository.save(existingClass);
                    return existingClass;
                })
                .orElseThrow(() -> new ResourceNotFoundException(SCHOOL_CLASS_NOT_FOUND_MSG + " with id: " + id));
    }

    @Override
    @Transactional
    public void deleteClass(final Long id) {
        SchoolClass schoolClass = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SCHOOL_CLASS_NOT_FOUND_MSG + " with id: " + id));
        repository.delete(schoolClass);
    }
}
