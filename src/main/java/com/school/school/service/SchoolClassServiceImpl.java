package com.school.school.service;

import com.school.school.model.SchoolClass;
import com.school.school.model.Student;
import com.school.school.repository.SchoolClassRepository;
import com.school.school.service.dto.SchoolClassDto;
import com.school.school.service.mapper.SchoolClassMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SchoolClassServiceImpl implements SchoolClassService {

    private final SchoolClassRepository repository;

    private final SchoolClassMapper mapper;

    @Override
    public List<SchoolClass> findAllClasses() {
        return repository.findAll();
    }

    @Override
    public List<SchoolClass> findAllSchoolClassesWithSubjects() {
        return repository.findAllWithSubjectsBy();
    }

    @Override
    public SchoolClass findClassById(final Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void createClass(final SchoolClassDto classDto) {
        repository.save(mapper.toEntity(classDto));
    }

    @Override
    public SchoolClass updateClass(final Long id, final SchoolClassDto classDto) {
        return repository.findById(id)
                .map(existingClass -> {
                    mapper.updateEntity(existingClass, classDto);
                    repository.save(existingClass);
                    return existingClass;
                })
                .orElse(null);
    }

    @Override
    public boolean deleteClass(final Long id) {
        return repository.findById(id)
                .map(schoolClass -> {
                    repository.delete(schoolClass);
                    return true;
                })
                .orElse(false);
    }
}
