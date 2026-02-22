package com.school.school.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.school.school.model.Student;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Функция поиска учащегося в базе данных по электронной почте.
     *
     * @param email электронная почта учащегося
     * @return найденный студент
     */
    Optional<Student> findByEmail(String email);

    /**
     * Функция поиска всех учащихся вместе с предметами.
     *
     * @return все учащиеся с загруженной коллекцией предметов
     */
    @EntityGraph(attributePaths = "subjects")
    List<Student> findAllWithSubjectsBy();
}
