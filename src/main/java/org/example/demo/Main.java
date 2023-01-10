package org.example.demo;

import org.example.domain.model.Student;
import org.example.persistence.ormanager.ORManager;
import org.example.persistence.utilities.Utils;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        String file = "h2.properties";
        ORManager manager = Utils.withPropertiesFrom(file);
        Student student = new Student("Shelly", "", 66, LocalDate.now());
        Student studentWithNullFields = new Student(null, null, null, null);

        manager.register(Student.class);

        manager.save(student);
        manager.save(studentWithNullFields);
    }
}