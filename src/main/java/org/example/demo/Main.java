package org.example.demo;

import org.example.domain.model.Academy;
import org.example.domain.model.Student;
import org.example.persistence.ormanager.ORManager;
import org.example.persistence.utilities.Utils;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        //todo to be deleted
        String path = "h2.properties";
        ORManager orManager = Utils.withPropertiesFrom(path);

        orManager.register(Academy.class,Student.class );
        Academy academy = new Academy("SoftServe");
        Academy academy1 = new Academy("Telerik");
        Academy academy2 = new Academy("SoftUni");
        orManager.save(academy);
        orManager.save(academy1);
        orManager.save(academy2);
        Student student = new Student("Neo", "The One", 999, LocalDate.parse("1999-03-24"));
        Student student1 = new Student("Neo1", "The One", 999, LocalDate.parse("1999-03-24"));
        Student student2 = new Student("Neo2", "The One", 999, LocalDate.parse("1999-03-24"));
        System.out.println(student);
        student.setAcademy(academy);
        student1.setAcademy(academy2);

        orManager.save(student);
        orManager.save(student1);
        orManager.save(student2);

        orManager.findById(1, Student.class);
        orManager.findAll(Student.class);

        student.setAge(44);
        orManager.update(student);


        System.out.println(academy.getStudents());
        System.out.println(academy1.getStudents());
        System.out.println(academy2.getStudents());

    }
}