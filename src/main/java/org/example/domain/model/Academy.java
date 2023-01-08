package org.example.domain.model;

import lombok.Data;
import org.example.persistence.annotations.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "academies")
public class Academy implements Serializable {
    @Id
    @Column(name = "id")
    Long id;
    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "academies")
    List<Student> students;

    Academy() {
    }

    public Academy(String name) {
        this.name = name;
        students = new ArrayList<>();
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }
}
