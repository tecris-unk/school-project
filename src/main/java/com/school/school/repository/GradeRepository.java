package com.school.school.repository;

import com.school.school.model.Grade;
import com.school.school.model.Student;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    @Query("""
    SELECT g FROM Grade g
    WHERE g.student IN :students AND g.date = :date
    """)
    List<Grade> findGradesByStudentsAndDate(@Param("students") List<Student> students, @Param("date") LocalDate date);
}
