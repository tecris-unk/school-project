package com.school.school.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.school.school.model.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByEmail(String email);
}
