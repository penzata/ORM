package org.example.domain.model;

import lombok.Data;
import org.example.persistence.annotations.Column;
import org.example.persistence.annotations.Entity;
import org.example.persistence.annotations.Id;
import org.example.persistence.annotations.Table;

@Data
@Entity
@Table(name = "students")
public class Student {

    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "first_name", nullable = false)
    private String firstName;

    public Student(String firstName) {
        this.firstName = firstName;
    }

    private Student() {
    }

}
