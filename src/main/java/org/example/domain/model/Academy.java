package org.example.domain.model;

import lombok.Data;
import org.example.persistence.annotations.Column;
import org.example.persistence.annotations.Entity;
import org.example.persistence.annotations.Id;
import org.example.persistence.annotations.Table;

import java.io.Serializable;

@Data
@Entity
@Table(name = "academies")
public class Academy implements Serializable {
    @Id
    @Column(name = "id")
    Long id;
    @Column(name = "name", nullable = false)
    private String name;

//    @OneToMany(mappedBy = "academy")
//    List<Student> students;

    Academy() {
    }

    public Academy(String name) {
        this.name = name;
    }
}
