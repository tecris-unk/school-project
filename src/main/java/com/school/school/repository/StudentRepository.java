package com.school.school.repository;

import com.school.school.model.Grade;
import com.school.school.model.Student;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("""
    SELECT st FROM Student st
    WHERE (:email IS NULL OR st.email = :email)
    ORDER BY st.id
    """)
    Page<Student> findStudentsByEmail(@Param("email") String email, Pageable pageable);

    @Query("""
        SELECT DISTINCT st FROM Student st
        LEFT JOIN FETCH st.grades g
        WHERE (:email IS NULL OR st.email = :email)
        ORDER BY st.id
        """)
    List<Student> findStudentsWithGrades(@Param("email") String email);

    @EntityGraph()
    List<Student> findAllWithGradesBy();

    Optional<Student> findByEmail(String email);
}
