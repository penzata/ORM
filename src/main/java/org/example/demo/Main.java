package org.example.demo;

import org.example.domain.model.Academy;
import org.example.domain.model.Student;
import org.example.persistence.ormanager.ORManager;
import org.example.persistence.utilities.Utils;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        String path = "h2.properties";
        ORManager orManager = Utils.withPropertiesFrom(path);

        orManager.register(Academy.class, Student.class);
        Academy academy = new Academy("SoftServe");
        orManager.save(academy);
        Student student = new Student("Neo", "The One", 999, LocalDate.parse("1999-03-24"));
        student.setAcademy(academy);
        orManager.save(student);
//        orManager.findById(2, Student.class);
//        orManager.findAll(Student.class);
        student.setAge(44);
        orManager.update(student);
    }
}