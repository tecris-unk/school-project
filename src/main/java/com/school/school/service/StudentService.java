package com.school.school.service;

import com.school.school.model.Grade;
import com.school.school.model.Student;
import com.school.school.service.dto.StudentDTO;

import java.util.List;

public interface StudentService {

    /**
     * Функция, возвращающая всех учащихся.
     *
     * @return все учащиеся, если таковы есть
     */
    List<Student> findAllStudents();

    /**
     * Функция, возвращающая всех учащихся вместе с предметами.
     *
     * @return все учащиеся с загруженной коллекцией предметов
     */
    List<Student> findAllStudentsWithSubjects();

    /**
     * Функция, создающая учащегося.
     *
     * @param student учашийся DTO
     */
    void createStudent(StudentDTO student);

    /**
     * Функция по нахождению учащегося по индетификатору.
     *
     * @param id интедификатор учащегося
     * @return найденный учащийся
     */
    Student findStudentById(Long id);

    /**
     * Функция по нахождению учащегося по электронной почте.
     *
     * @param email электронная почта учащегося
     * @return найденный учащийся
     */
    Student findStudentByEmail(String email);

    /**
     * Функция по обновлению учащегося.
     *
     * @param id индетификатор учащегося, которого нужно изменить.
     * @param updatedStudent обновление учащегося DTO
     * @return обновленный учащийся
     */
    Student updateStudent(Long id, StudentDTO updatedStudent);

    /**
     * Фукнция по удалению учащегося.
     *
     * @param id индетификатор учащегося, подлежащего удалению.
     * @return удален ли пользователь ?
     */
    boolean deleteStudent(Long id);

    /**
     * Функция по созданию учащегося с оценками.
     *
     * @param student учащийся DTO
     * @param grades оценки учащегося
     */
    void createStudentWithGrades(StudentDTO student, List<Grade> grades);
}
