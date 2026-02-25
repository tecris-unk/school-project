package com.school.school.repository;

import com.school.school.model.SchoolClass;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
    @EntityGraph(attributePaths = {"subjects"})
    List<SchoolClass> findAllWithSubjectsBy();
}
