package com.school.school.service;

import com.school.school.model.SchoolClass;
import com.school.school.service.dto.SchoolClassDTO;
import java.util.List;

public interface SchoolClassService {

    List<SchoolClass> findAllClasses();

    SchoolClass findClassById(Long id);

    void createClass(SchoolClassDTO classDTO);

    SchoolClass updateClass(Long id, SchoolClassDTO classDTO);

    boolean deleteClass(Long id);
}
