package org.example.persistence.utilities;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.model.Student;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.List;

@Slf4j
class SerializationUtilTest {

    static String fileName;
    Student student;

    @BeforeAll
    static void init() {
        fileName = "serTest.ser";
    }

    @BeforeEach
    void setUp() {
        student = new Student("Jorji");
        student.setId(3L);
    }

    @AfterEach
    void tearDown() {
    }

    @RepeatedTest(10)
    void SerializationTest() {
        SerializationUtil.serialize(student, fileName);

        log.atDebug().log("before serialization: {}", student);

    }

    @Test
    void DeserializationTest() {
        List<Object> emptyStudentsList = null;
        emptyStudentsList = SerializationUtil.deserialize(fileName);

        log.atDebug().log("after deserialization: {}", emptyStudentsList);
    }
}