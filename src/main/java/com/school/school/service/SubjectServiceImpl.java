package com.school.school.service;

import com.school.school.exceptions.ResourceNotFoundException;
import com.school.school.model.Subject;
import com.school.school.model.Teacher;
import com.school.school.repository.SubjectRepository;
import com.school.school.repository.TeacherRepository;
import com.school.school.service.dto.SubjectDto;
import com.school.school.service.mapper.SubjectMapper;
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
    public List<Subject> findAllSubjects() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Subject findSubjectById(final Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SUBJECT_NOT_FOUND_MSG + " with id: " + id));
    }

    @Override
    @Transactional
    public void createSubject(final SubjectDto subjectDto) {
        Subject subject = mapper.toEntity(subjectDto);
        if (subjectDto.getTeacherId() != null) {
            Teacher teacher = teacherRepository
                    .findById(subjectDto.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException(TEACHER_NOT_FOUND_MSG + " with id: " + subjectDto.getTeacherId()));
            subject.setTeacher(teacher);
        }
        repository.save(subject);
    }

    @Override
    @Transactional
    public Subject updateSubject(final Long id, final SubjectDto subjectDto) {
        return repository.findById(id)
                .map(existingSubject -> {
                    mapper.updateEntity(existingSubject, subjectDto);
                    if (subjectDto.getTeacherId() == null) {
                        existingSubject.setTeacher(null);
                    } else {
                        Teacher teacher = teacherRepository
                                .findById(subjectDto.getTeacherId())
                                .orElseThrow(() -> new ResourceNotFoundException(TEACHER_NOT_FOUND_MSG + " with id: " + subjectDto.getTeacherId()));
                        existingSubject.setTeacher(teacher);
                    }
                    repository.save(existingSubject);
                    return existingSubject;
                })
                .orElseThrow(() -> new ResourceNotFoundException(SUBJECT_NOT_FOUND_MSG + " with id: " + id));
    }

    @Override
    @Transactional
    public void deleteSubject(final Long id) {
        Subject subject = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SUBJECT_NOT_FOUND_MSG + " with id: " + id));
        repository.delete(subject);
    }
}
