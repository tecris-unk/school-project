package com.school.school.repository;

import com.school.school.model.SchoolClass;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    List<SchoolClass> findAllWithSubjectsBy();

    boolean existsByGradeAndLetterIgnoreCase(Integer grade, String letter);

    boolean existsByGradeAndLetterIgnoreCaseAndIdNot(Integer grade, String letter, Long id);
}
