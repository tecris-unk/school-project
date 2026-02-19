package com.school.school.repository;

import com.school.school.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    /**
     * Функция, существует ли учащийся с такой электронной почтой.
     *
     * @param email электронная почта
     * @return найден ли студент
     */
    boolean existsByEmail(String email);
}
