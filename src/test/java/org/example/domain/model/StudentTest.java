package org.example.domain.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class StudentTest {
    Student st1;
    Student st2;

    @BeforeEach
    void setUp() {
        st1 = new Student("Bob");
        st2 = new Student("Dale");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("checking if Lombok getters & setters work correct")
    void GettersAndSettersCheck() {
        assertEquals("Bob", st1.getFirstName());
        assertNull(st1.getId());

        st1.setFirstName("Bobby");
        assertEquals("Bobby", st1.getFirstName());
        assertNotEquals("Bob", st1.getFirstName());

        st1.setId(3L);
        assertEquals(3, st1.getId());
        assertNotEquals(null, st1.getId());
    }

    @Test
    @DisplayName("checking if Lombok equals and hashcode methods work correct")
    void EqualsAndHashCodeCheck() {
        boolean initialResult = st1.hashCode() == st2.hashCode();
        log.atDebug().log("st1's hashcode: {}\nst2's hashcode: {}", st1.hashCode(), st2.hashCode());
        assertFalse(initialResult);

        assertNotSame(st1, st2);
        assertNotEquals(st1, st2);

        st2.setFirstName("Bob");

        assertNotSame(st1, st2);
        assertEquals(st1, st2);

        boolean afterChangeResult = st1.hashCode() == st2.hashCode();
        log.atDebug().log("st1's hashcode: {}\nst2's hashcode: {}", st1.hashCode(), st2.hashCode());
        assertTrue(afterChangeResult);
    }

    @Test
    void toStringMethodCheck() {
        log.atDebug().log("student1: {}", st1);
        log.atDebug().log("student2: {}", st2);
    }
}