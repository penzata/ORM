package org.example.persistence.utilities;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.model.Student;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class SerializationUtilTest {

    @RepeatedTest(3)
    void serialization() {
        Student st = new Student("Jorji", "Jo", 27, LocalDate.now());
        st.setId(3L);
        SerializationUtil.serialize(st, "tests");

        log.atDebug().log("before serialization: {}, {}", st, st.hashCode());
    }

    @Test
    void deserialization() {
        List<Object> emptyStudentsList = null;
        emptyStudentsList = SerializationUtil.deserialize("tests");

        log.atDebug().log("after deserialization: {}", emptyStudentsList);
    }

    @Test
    void WhenSerializeAndTheDeserializeObjectThenReturnThatTheyAreEqual() {
        Student student = new Student("Jack", "Black", 53, LocalDate.now());
        student.setId(33L);
        Student student2 = new Student("Kyle", "Gass", 62, LocalDate.now());
        student.setId(248L);

        SerializationUtil.serialize(student, "tests");
        SerializationUtil.serialize(student2, "tests");
        List<Object> deserializedStudents = SerializationUtil.deserialize("tests");

        assertThat(student).usingRecursiveComparison().isEqualTo(deserializedStudents.get(0));
        assertThat(student).isEqualTo(deserializedStudents.get(0));
        assertThat(student2).usingRecursiveComparison().isEqualTo(deserializedStudents.get(1));
        assertThat(student2).isEqualTo(deserializedStudents.get(1));

        log.atDebug().log("student's hashcode: {}\n" +
                        "student2's hashcode: {}\n" +
                        "deserializedStudents' hashcode: {}, {}",
                student.hashCode(), student2.hashCode(),
                deserializedStudents.get(0).hashCode(), deserializedStudents.get(1).hashCode());

        log.atDebug().log("{}", deserializedStudents);
    }

}