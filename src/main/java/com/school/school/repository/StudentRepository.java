package com.school.school.repository;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import com.school.school.model.Student;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<@NonNull Student, @NonNull Long> {
    Optional<Student> findByEmail(String email);
}
