package org.example.domain.model;

import org.example.persistence.annotations.Column;
import org.example.persistence.annotations.Entity;
import org.example.persistence.annotations.Id;
import org.example.persistence.annotations.Table;

@Entity
@Table(name = "academies")
public class Academy {
    @Id
    @Column(name = "id")
    Long id;
    @Column(name = "name", nullable = false)
    private String name;

//    @OneToMany(mappedBy = "academy_id")
//    List<Student> students;

    public Academy(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
