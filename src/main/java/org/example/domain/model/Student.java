package org.example.domain.model;

import lombok.Data;
import org.example.persistence.annotations.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@Entity
@Table(name = "students")
public class Student implements Serializable {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @ManyToOne(targetEntity = SchoolClass.class, name ="school_class_id")
    private SchoolClass schoolClass;

    public Student(String firstName) {
        this.firstName = firstName;
    }

    Student() {
    }

}