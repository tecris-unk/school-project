package com.school.school.repository;

import com.school.school.model.Student;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @EntityGraph(attributePaths = "grades")
    List<Student> findAllWithGradesBy();

    Optional<Student> findByEmail(String email);
}
