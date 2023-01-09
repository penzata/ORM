package org.example.domain.model;

import lombok.Data;
import org.example.persistence.annotations.*;

import java.io.Serializable;
import java.util.ArrayList;

@Data
@Entity
@Table(name = "academies")
public class Academy implements Serializable {
    @Id
    @Column(name = "id")
    Long id;
    @OneToMany(mappedBy = "academies")
    ArrayList<Student> students = new ArrayList<>();
    @Column(name = "name", nullable = false)
    private String name;

    Academy() {
    }

    public Academy(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Academy{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}