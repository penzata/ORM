package org.example.demo;

import org.example.domain.model.SchoolClass;
import org.example.domain.model.Student;
import org.example.persistence.ormanager.ORManager;
import org.example.persistence.utilities.Utils;

public class Main {
    public static void main(String[] args) {
        String path = "h2.properties";
        ORManager orManager = Utils.withPropertiesFrom(path);

        orManager.register(SchoolClass.class);
        orManager.register(Student.class);
        orManager.save(new Student("Ivan"));
        orManager.findById(2L, Student.class);
        orManager.findAll(Student.class);
    }
}