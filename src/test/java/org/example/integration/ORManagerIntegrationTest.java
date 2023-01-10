package org.example.integration;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.assertj.db.type.Table;
import org.example.domain.model.Academy;
import org.example.domain.model.Student;
import org.example.exceptionhandler.EntityNotFoundException;
import org.example.persistence.ormanager.ORManager;
import org.example.persistence.utilities.Utils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.db.output.Outputs.output;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class ORManagerIntegrationTest {
    static ORManager manager;
    static HikariDataSource dataSource;
    static Connection connection;
    static PreparedStatement ps;
    static Table createdStudentsTable;
    static Table createdAcademiesTable;

    @AfterAll
    static void tearDown() throws SQLException {
        ps = connection.prepareStatement("DROP TABLE IF EXISTS students");
        ps.executeUpdate();
        ps = connection.prepareStatement("DROP TABLE IF EXISTS academies");
        ps.executeUpdate();

        if (connection != null) {
            connection.close();
        }
        if (dataSource != null) {
            dataSource.close();
        }
        if (ps != null) {
            ps.close();
        }
    }

    @BeforeAll
    static void setUp() throws SQLException {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test");
        manager = Utils.withDataSource(dataSource);
        manager.register(Academy.class, Student.class);
        connection = dataSource.getConnection();
        createdStudentsTable = new Table(dataSource, "students");
        createdAcademiesTable = new Table(dataSource, "academies");
    }

    @Test
    @DisplayName("One big test for different interactions between ORM Manager methods")
    void MethodsFlow() throws SQLException {
        log.atDebug().log("1) Creating 7 Students & 3 Academies");
        Student student1 = new Student("Don", "Johnson", 73, LocalDate.now());
        Student student2 = new Student("Emma", "Thompson", 63, LocalDate.now());
        Student student3 = new Student("Kurt", "Russell", 71, LocalDate.parse("1999-03-24"));
        Student student4 = new Student("Mel", "Gibson", 67, LocalDate.of(2021, 5, 5));
        Student student5 = new Student("Keanu", "Reeves", 58, LocalDate.now());
        Student student6 = new Student("John", "Travolta", 68, LocalDate.parse("2015-07-11"));
        Student student7 = new Student("Steve", "Buscemi", 65, LocalDate.now());
        Academy academy1 = new Academy("SoftServe");
        Academy academy2 = new Academy("Khan");
        Academy academy3 = new Academy("Atlas");

        log.atDebug().log("2) Persisting Academy1 & 3 to DB");
        manager.persist(academy1);
        manager.persist(academy3);

        log.atDebug().log("3) Setting academies to students, except to Student5 & 6");
        student1.setAcademy(academy3);
        student2.setAcademy(academy3);
        student3.setAcademy(academy2);
        student4.setAcademy(academy2);
        student7.setAcademy(academy3);

        log.atDebug().log("4) Persisting all students to DB");
        manager.persist(student1);
        manager.persist(student2);
        manager.persist(student3);
        manager.persist(student4);
        manager.persist(student5);
        manager.persist(student6);
        manager.persist(student7);

        log.atDebug().log("5) Even if Student4 has academy it was never saved in DB, so assert that academy ID is null");
        assertThat(student4.getAcademy().getId()).isNull();

        log.atDebug().log("6) Changing Student2 academy name and assert that it's changed");
        student2.setAcademy(academy1);
        manager.update(student2);
        String academyName = student2.getAcademy().getName();

        assertEquals("SoftServe", academyName);

        log.atDebug().log("7) Saving the second academy to DB and assert that have an ID");
        manager.save(academy2);

        assertEquals(3, academy2.getId());

        log.atDebug().log("8) Updating Student6 academy and assert that academy_id field is not null");
        student6.setAcademy(academy1);
        manager.update(student6);

        assertThat(student6.getAcademy()).isNotNull();

        log.atDebug().log("9) Delete Student5 and assert true");
        boolean deleteResult = manager.delete(student5);

        assertTrue(deleteResult);

        log.atDebug().log("10) Assert that deleted Student5's ID is null");
        assertThat(student5.getId()).isNull();

        log.atDebug().log("11) Update deleted Student5 and assert that throws exception");
        assertThrows(EntityNotFoundException.class, () -> manager.update(student5));

        log.atDebug().log("12) Persist a new Student8 and assert that the assigned ID is greater than the last saved student");
        Student student8 = new Student("Melanie", "Griffith", 65, LocalDate.now());
        student8.setAcademy(academy3);

        manager.persist(student8);

        assertThat(student8.getId()).isGreaterThan(student7.getId());

        log.atDebug().log("12) Update Student3 & 4 at DB side");
        String dbUpdatedStudent3 = """
                UPDATE students
                SET second_name = 'Vonnegut', age = 84, graduate_academy = '2007-04-11'
                WHERE id = 3
                """;
        connection.prepareStatement(dbUpdatedStudent3).executeUpdate();
        String dbUpdatedStudent4 = """
                UPDATE students
                SET first_name = 'William', age = 74, graduate_academy = '2001-01-01'
                WHERE id = 4
                """;
        connection.prepareStatement(dbUpdatedStudent4).executeUpdate();

        log.atDebug().log("13) Refresh Student3 & 4 and assert that properties correctly changed");
        manager.refresh(student3);
        manager.refresh(student4);

        assertThat(student3.getSecondName()).isEqualTo("Vonnegut");
        assertThat(student4.getGraduateAcademy()).isBefore(LocalDate.of(2001, 1, 2));

        log.atDebug().log("14) FindById Student2 & delete it");
        Student foundById = manager.findById(2, Student.class).get();
        manager.delete(foundById);

        log.atDebug().log("15) FindAll students and assert that they are 6");
        List<Student> allStudents = manager.findAll(Student.class);

        assertEquals(6, allStudents.size());

        log.atDebug().log("16) FindById Student3 & 4, set Academies to Academy2");
        Student foundStudent3 = manager.findById(student3.getId(), Student.class).get();
        foundStudent3.setAcademy(academy2);
        Student foundStudent4 = manager.findById(student4.getId(), Student.class).get();
        foundStudent4.setAcademy(academy2);

        log.atDebug().log("17) Update Student3 & 4 and assert that the academy field is not null");
        manager.update(student3);
        manager.update(student4);

        assertThat(student3.getAcademy()).isNotNull();
        assertThat(student4.getAcademy()).isNotNull();

        log.atDebug().log("18) FindById that doesnt exist in Academy table and assert that returns empty optional ");
        Optional<Academy> byId = manager.findById(4, Academy.class);

        assertThat(byId).isNotPresent();


        output(createdStudentsTable).toConsole();
        output(createdAcademiesTable).toConsole();
    }

}