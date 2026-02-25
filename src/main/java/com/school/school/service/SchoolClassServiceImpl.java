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
    public List<SchoolClassDto> findAllClasses() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolClassDto> findAllSchoolClassesWithSubjects() {
        return repository.findAllWithSubjectsBy().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolClassDto findClassById(final Long id) {
        SchoolClass schoolClass = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SCHOOL_CLASS_NOT_FOUND_MSG + " with id: " + id));
        return mapper.toDto(schoolClass);
    }

    @Override
    public SchoolClassDto createClass(final SchoolClassDto classDto) {
        SchoolClass schoolClass = repository.save(mapper.toEntity(classDto));
        return mapper.toDto(schoolClass);
    }

    @Override
    @Transactional
    public SchoolClassDto updateClass(final Long id, final SchoolClassDto classDto) {
        SchoolClass schoolClass = repository.findById(id)
                .map(existingClass -> {
                    mapper.updateEntity(existingClass, classDto);
                    return repository.save(existingClass);
                })
                .orElseThrow(() -> new ResourceNotFoundException(SCHOOL_CLASS_NOT_FOUND_MSG + " with id: " + id));
        return mapper.toDto(schoolClass);
    }

    @Override
    @Transactional
    public void deleteClass(final Long id) {
        SchoolClass schoolClass = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SCHOOL_CLASS_NOT_FOUND_MSG + " with id: " + id));
        repository.delete(schoolClass);
    }
}
