package org.example.domain.model;

import lombok.Data;
import org.example.persistence.annotations.Column;
import org.example.persistence.annotations.Entity;
import org.example.persistence.annotations.Id;
import org.example.persistence.annotations.Table;

@Data
@Entity
@Table(name = "academies")
public class Academy {
    @Id
    @Column(name = "id")
    Long id;
    @Column(name = "name", nullable = false)
    private String name;

//    @OneToMany(mappedBy = "academy")
//    List<Student> students;

    public Academy(String name) {
        this.name = name;
    }
}
