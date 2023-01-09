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
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;
    @OneToMany(mappedBy = "academies")
    private List<Student> students = new ArrayList<>();

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