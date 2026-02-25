package com.school.school.service;

import com.school.school.service.dto.SchoolClassDto;
import java.util.List;

public interface SchoolClassService {

    List<SchoolClassDto> findAllClasses();

    List<SchoolClassDto> findAllSchoolClassesWithSubjects();

    SchoolClassDto findClassById(Long id);

    SchoolClassDto createClass(SchoolClassDto classDto);

    SchoolClassDto updateClass(Long id, SchoolClassDto classDto);

    void deleteClass(Long id);
}
