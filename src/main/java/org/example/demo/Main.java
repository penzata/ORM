package org.example.demo;

import org.example.domain.model.SchoolClass;
import org.example.domain.model.Student;
import org.example.persistence.ormanager.ORManager;
import org.example.persistence.utilities.Utils;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        String path = "h2.properties";
        ORManager orManager = Utils.withPropertiesFrom(path);

        orManager.register(SchoolClass.class);
        orManager.register(Student.class);
        SchoolClass someClass = new SchoolClass("12A");
        orManager.save(someClass);
        orManager.save(new Student("Neo", "The One", 999, LocalDate.parse("1999-03-24"), someClass));
        orManager.findById(2, Student.class);
        orManager.findAll(Student.class);
    }
}