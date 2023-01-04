package org.example.domain.model;

import lombok.Data;
import org.example.persistence.annotations.Column;
import org.example.persistence.annotations.Entity;
import org.example.persistence.annotations.Id;
import org.example.persistence.annotations.Table;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "students")
public class Student implements Serializable {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Column(name = "second_name", nullable = false)
    private String secondName;
    @Column(name = "age", nullable = false)
    private int age;
    @Column(name = "graduate_academy")
    private LocalDate graduateAcademy;

    Student() {
    }

    public Student(String firstName, String secondName, int age, LocalDate graduateAcademy) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.age = age;
        this.graduateAcademy = graduateAcademy;
    }
}