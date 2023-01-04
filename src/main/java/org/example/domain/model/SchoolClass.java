package org.example.domain.model;

import lombok.Data;
import org.example.persistence.annotations.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "school_class")
public class SchoolClass {
    @Id
    @Column(name = "id")
    Long id;
    @Column(name = "name", nullable = false)
    private String name;

//    @OneToMany(mappedBy = "school_class")
//    List<Student> students;


    public SchoolClass(String name) {
        this.name = name;
    }

    public String getName() {
        return "school_class";
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
