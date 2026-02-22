package com.school.school.service;

import com.school.school.model.Subject;
import com.school.school.model.Teacher;
import com.school.school.repository.SubjectRepository;
import com.school.school.repository.TeacherRepository;
import com.school.school.service.dto.SubjectDTO;
import com.school.school.service.mapper.SubjectMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository repository;

    private final TeacherRepository teacherRepository;

    private final SubjectMapper mapper;

    @Override
    public List<Subject> findAllSubjects() {
        return repository.findAll();
    }

    @Override
    public Subject findSubjectById(final Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Subject createSubject(final SubjectDTO subjectDTO) {
        Subject subject = mapper.toEntity(subjectDTO);
        if (subjectDTO.getTeacherId() != null) {
            Teacher teacher = teacherRepository.
                    findById(subjectDTO.getTeacherId()).orElse(null);
            if (teacher == null) {
                return null;
            }
            subject.setTeacher(teacher);
        }
        repository.save(subject);
        return subject;
    }

    @Override
    public Subject updateSubject(final Long id, final SubjectDTO subjectDTO) {
        return repository.findById(id)
                .map(existingSubject -> {
                    mapper.updateEntity(existingSubject, subjectDTO);
                    if (subjectDTO.getTeacherId() == null) {
                        existingSubject.setTeacher(null);
                    } else {
                        Teacher teacher = teacherRepository.
                                findById(subjectDTO.getTeacherId()).orElse(null);
                        if (teacher == null) {
                            return null;
                        }
                        existingSubject.setTeacher(teacher);
                    }
                    repository.save(existingSubject);
                    return existingSubject;
                })
                .orElse(null);
    }

    @Override
    public boolean deleteSubject(final Long id) {
        return repository.findById(id)
                .map(subject -> {
                    repository.delete(subject);
                    return true;
                })
                .orElse(false);
    }
}
