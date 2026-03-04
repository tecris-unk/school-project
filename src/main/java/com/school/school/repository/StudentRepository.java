package com.school.school.repository;

import com.school.school.model.Student;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query(value = """
            SELECT DISTINCT st.id FROM Student st
            LEFT JOIN st.grades g
            LEFT JOIN g.subject sb
            LEFT JOIN sb.teacher t
            WHERE (:teacherEmail IS NULL OR t.email = :teacherEmail)
               AND (:subjectName IS NULL OR LOWER(sb.name) LIKE CONCAT('%', CAST(:subjectName AS string), '%'))
              AND (:minScore IS NULL OR g.score >= :minScore)
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT st.id) FROM Student st
                    LEFT JOIN st.grades g
                    LEFT JOIN g.subject sb
                    LEFT JOIN sb.teacher t
                    WHERE (:teacherEmail IS NULL OR t.email = :teacherEmail)
                      AND (:subjectName IS NULL OR LOWER(sb.name) LIKE CONCAT('%', CAST(:subjectName AS string), '%'))
                      AND (:minScore IS NULL OR g.score >= :minScore)
                    """)
    Page<Long> findStudentIdsByNestedFiltersJpql(
            @Param("teacherEmail") String teacherEmail,
            @Param("subjectName") String subjectName,
            @Param("minScore") Integer minScore,
            Pageable pageable
    );

    @Query(value = """
            SELECT DISTINCT st.id
            FROM students st
            LEFT JOIN grades g ON g.student_id = st.id
            LEFT JOIN subjects sb ON sb.id = g.subject_id
            LEFT JOIN teachers t ON t.id = sb.teacher_id
            WHERE (:teacherEmail IS NULL OR t.email = :teacherEmail)
               AND (:subjectName IS NULL OR sb.name ILIKE CONCAT('%', CAST(:subjectName AS TEXT), '%'))
              AND (:minScore IS NULL OR g.score >= :minScore)
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT st.id)
                    FROM students st
                    LEFT JOIN grades g ON g.student_id = st.id
                    LEFT JOIN subjects sb ON sb.id = g.subject_id
                    LEFT JOIN teachers t ON t.id = sb.teacher_id
                    WHERE (:teacherEmail IS NULL OR t.email = :teacherEmail)
                      AND (:subjectName IS NULL OR sb.name ILIKE CONCAT('%', CAST(:subjectName AS TEXT), '%'))
                      AND (:minScore IS NULL OR g.score >= :minScore)
                    """,
            nativeQuery = true)
    Page<Long> findStudentIdsByNestedFiltersNative(
            @Param("teacherEmail") String teacherEmail,
            @Param("subjectName") String subjectName,
            @Param("minScore") Integer minScore,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT st FROM Student st
            LEFT JOIN FETCH st.grades g
            LEFT JOIN FETCH g.subject sb
            LEFT JOIN FETCH sb.teacher t
            WHERE st.id IN :ids
            """)
    List<Student> findAllByIdsWithGradesAndTeacher(@Param("ids") List<Long> ids);

    Optional<Student> findByEmail(String email);
}