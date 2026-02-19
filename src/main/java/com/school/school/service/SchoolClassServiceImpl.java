package com.school.school.service;

import com.school.school.model.SchoolClass;
import com.school.school.repository.SchoolClassRepository;
import com.school.school.service.dto.SchoolClassDTO;
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
    public SchoolClass findClassById(final Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void createClass(final SchoolClassDTO classDTO) {
        repository.save(mapper.toEntity(classDTO));
    }

    @Override
    public SchoolClass updateClass(final Long id, final SchoolClassDTO classDTO) {
        return repository.findById(id)
                .map(existingClass -> {
                    mapper.updateEntity(existingClass, classDTO);
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
