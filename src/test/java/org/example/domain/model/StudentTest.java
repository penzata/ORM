package org.example.domain.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class StudentTest {
    Student student1;
    Student student2;

    @BeforeEach
    void setUp() {
        student1 = new Student("Bob", "just Bob", 100, LocalDate.now());
        student2 = new Student("Dale", "Cooper", 33, LocalDate.now());
    }

    @Test
    @DisplayName("checking if Lombok getters & setters work correct")
    void LombokGettersAndSettersCheck() {
        assertEquals("Bob", student1.getFirstName());
        assertNull(student1.getId());

        student1.setFirstName("Bobby");

        assertEquals("Bobby", student1.getFirstName());
        assertNotEquals("Bob", student1.getFirstName());

        student1.setId(3L);

        assertEquals(3, student1.getId());
        assertNotEquals(null, student1.getId());
    }

    @Test
    @DisplayName("checking if Lombok equals and hashcode methods work correct")
    void LombokEqualsAndHashCodeCheck() {
        boolean initialResult = student1.hashCode() == student2.hashCode();
        log.atDebug().log("st1's hashcode: {}\nst2's hashcode: {}", student1.hashCode(), student2.hashCode());

        assertFalse(initialResult);
        assertNotSame(student1, student2);
        assertNotEquals(student1, student2);

        student2.setFirstName("Bob");
        student2.setSecondName("just Bob");
        student2.setAge(100);
        boolean afterChangeResult = student1.hashCode() == student2.hashCode();
        log.atDebug().log("st1's hashcode: {}\nst2's hashcode: {}", student1.hashCode(), student2.hashCode());

        assertTrue(afterChangeResult);
        assertNotSame(student1, student2);
        assertEquals(student1, student2);
    }

    @Test
    void LombokToStringMethodCheck() {
        log.atDebug().log("student1: {}", student1);
        log.atDebug().log("student2: {}", student2);

        assertNotEquals(student1.toString(), student2.toString());

        student2.setFirstName("Bob");
        student2.setSecondName("just Bob");
        student2.setAge(100);

        assertEquals(student1.toString(), student2.toString());
    }

    @Test
    void WhenUsingNoArgsConstructorReturnsObjectWithNullFields() {
        Student student = new Student();

        assertNull(student.getId());
        assertNull(student.getFirstName());
        assertNull(student.getSecondName());
        assertEquals(null, student.getAge());
        assertNull(student.getGraduateAcademy());
    }

}