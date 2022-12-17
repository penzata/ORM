package org.example.business.model;

import lombok.Getter;
import lombok.Setter;
import org.example.persistence.annotations.Id;

@Getter @Setter
@Entity
@Table(name = "students")
public class Student {

    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "first_name")
    private String firstName;


    public Student(String firstName) {
        this.firstName = firstName;
    }

    private Student() {
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + firstName + '\'' +
                '}';
    }
}
