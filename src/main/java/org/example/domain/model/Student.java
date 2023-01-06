package org.example.domain.model;

import lombok.Data;
import org.example.persistence.annotations.*;

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
    private Integer age;
    @Column(name = "graduate_academy")
    private LocalDate graduateAcademy;
    @ManyToOne(targetEntity = Academy.class, name = "academy_id")
    private Academy academy;

    Student() {
    }

    public Student(String firstName, String secondName, Integer age, LocalDate graduateAcademy) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.age = age;
        this.graduateAcademy = graduateAcademy;
    }

}