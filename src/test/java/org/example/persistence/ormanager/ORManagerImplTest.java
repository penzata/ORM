package org.example.persistence.ormanager;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.assertj.db.type.Table;
import org.example.domain.model.Student;
import org.example.persistence.annotations.Column;
import org.example.persistence.annotations.Entity;
import org.example.persistence.annotations.Id;
import org.example.persistence.utilities.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.db.api.Assertions.assertThat;
import static org.assertj.db.output.Outputs.output;

@Slf4j
class ORManagerImplTest {
    ORManager manager;
    HikariDataSource dataSource;
    Connection connection;
    PreparedStatement ps;
    Table createdStudentsTable;
    Student student1;

    @AfterEach
    void tearDown() throws SQLException {
        ps = connection.prepareStatement("DROP TABLE students");
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

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test");
        manager = Utils.withDataSource(dataSource);
        manager.register(Student.class);
        connection = dataSource.getConnection();
        createdStudentsTable = new Table(dataSource, "students");
        student1 = new Student("Johny");
    }

    @Test
    void CanSaveOneStudentToDatabaseAndReturnStudentWithId() {
        Student savedStudent = manager.save(student1);

        assertThat(savedStudent.getId()).isNotNull();

        output(createdStudentsTable).toFile("tableFromTest");
    }

    @Test
    void CanSaveTwoStudentsToDatabaseAndReturnStudentsWithId() {
        Student savedStudent = manager.save(student1);
        Student savedBeavis = manager.save(new Student("Beavis"));

        assertThat(savedStudent.getId()).isPositive();
        assertThat(savedBeavis.getId()).isGreaterThan(savedStudent.getId());

        output(createdStudentsTable).toFile("tableFromTest");
    }

    @Test
    void WhenSavingExistingObjectIntoDatabaseThenReturnTheSameAndDontSaveIt() {
        Student st = new Student("Harry");
        manager.save(st);
        manager.save(st);
        manager.save(st);

        assertThat(createdStudentsTable).hasNumberOfRows(1);

        output(createdStudentsTable).toFile("tableFromTest");
    }

    @Test
    void canFindPersonById() {
        Student savedStudent = manager.save(new Student("Dick"));

        Optional<Student> foundStudent = manager.findById(savedStudent.getId(), Student.class);

        assertThat(foundStudent).isPresent();
        assertThat(foundStudent.get().getId()).isEqualTo(savedStudent.getId());
    }

    @Test
    void WhenIdDoesntExistsThenReturnNullableObject() {
        Optional<Student> personToBeFound = manager.findById(-1L, Student.class);

        assertThat(personToBeFound.get().getId()).isNull();
        assertThat(personToBeFound.get().getFirstName()).isNull();
    }

    @Test
    void WhenFindAllThenReturnAllSavedToDBObjects() {
        manager.save(new Student("Ivan"));
        manager.save(new Student("Petkan"));

        List<Student> allStudents = manager.findAll(Student.class);

        assertThat(allStudents).hasSize(2);
        assertThat(createdStudentsTable).row(1)
                .value().isEqualTo(2)
                .value().isEqualTo("Petkan");

        output(createdStudentsTable).toFile("tableFromTest");
    }

    @Test
    void WhenRegisterAnEntityReturnATableMatchingItsFields() {
        @Entity
        @org.example.persistence.annotations.Table(name = "trial_table")
        class TrialTable {
            @Id
            @Column(name = "trial_id")
            int trialId;
            @Column(name = "trial_first_name", nullable = false)
            String trialFirstName;
            @Column(nullable = false)
            boolean under18;
        }
        Table table = new Table(dataSource, "trial_table");

        manager.register(TrialTable.class);

        assertThat(table).hasNumberOfColumns(3);
        assertThat(table).column(1)
                .hasColumnName("trial_first_name");

        output(table).toFile("tableFromTest");
    }

    @Test
    void delete() {
        Student savedStudent = manager.save(new Student("Huhg"));

        manager.delete(savedStudent);


    }

}