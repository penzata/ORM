package org.example.persistence.utilities;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.model.Student;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j
class SerializationUtilTest {

    @RepeatedTest(3)
    void SerializationTest() {
        Student student = new Student("Jorji");
        student.setId(3L);
        SerializationUtil.serialize(student);

        log.atDebug().log("before serialization: {}, {}", student, student.hashCode());
    }

    @Test
    void DeserializationTest() {
        List<Object> emptyStudentsList = null;
        emptyStudentsList = SerializationUtil.deserialize(Student.class);

        log.atDebug().log("after deserialization: {}", emptyStudentsList);
    }
}