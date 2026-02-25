package com.school.school.service;

import com.school.school.model.SchoolClass;
import com.school.school.service.dto.SchoolClassDto;
import java.util.List;

public interface SchoolClassService {

    List<SchoolClass> findAllClasses();

    List<SchoolClass> findAllSchoolClassesWithSubjects();

    SchoolClass findClassById(Long id);

    void createClass(SchoolClassDto classDto);

    SchoolClass updateClass(Long id, SchoolClassDto classDto);

    void deleteClass(Long id);
}
