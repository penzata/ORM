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
    private int age;
    @Column(name = "graduate_academy")
    private LocalDate graduateAcademy;
    @ManyToOne(targetEntity = SchoolClass.class, name ="school_class_id")
    private SchoolClass schoolClass;
  
    Student() {
    }

    public Student(String firstName, String secondName, int age, LocalDate graduateAcademy) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.age = age;
        this.graduateAcademy = graduateAcademy;
    }
    public Student(String firstName, String secondName, int age, LocalDate graduateAcademy, SchoolClass schoolClass) {
        this(firstName, secondName, age, graduateAcademy);
        this.schoolClass = schoolClass;
    }
}