package com.school.school.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "school_classes",
        uniqueConstraints = @UniqueConstraint(name = "uk_school_class_grade_letter", columnNames = {"grade", "letter"})
)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SchoolClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer grade;

    private String letter;

    @OneToMany(
            mappedBy = "schoolClass",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY
    )
    private List<Student> students = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "school_classes_subjects",
            joinColumns = @JoinColumn(name = "school_class_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private List<Subject> subjects = new ArrayList<>();
}
