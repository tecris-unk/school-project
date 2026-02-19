package com.school.school.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.school.school.model.Student;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    /**
     * Функция поиска учащегося в базе данных по электронной почте.
     *
     * @param email электронная почта учащегося
     * @return найденный студент
     */
    Optional<Student> findByEmail(String email);
}
