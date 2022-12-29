package org.example.persistence.ormanager;

import com.zaxxer.hikari.HikariDataSource;
import org.assertj.db.type.Table;
import org.assertj.db.type.ValueType;
import org.example.domain.model.Student;
import org.example.persistence.annotations.Column;
import org.example.persistence.annotations.Entity;
import org.example.persistence.annotations.Id;
import org.example.persistence.utilities.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.db.api.Assertions.assertThat;
import static org.assertj.db.output.Outputs.output;

class ORManagerImplTest {
    static ORManager manager;
    static HikariDataSource dataSource;
    static Connection connection;
    static Table createdTable;
    Student student1;

    @BeforeAll
    static void init() {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test");
        manager = Utils.withDataSource(dataSource);
        manager.register(Student.class);
        createdTable = new Table(dataSource, "students");
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        connection = dataSource.getConnection();
        student1 = new Student("Johny");
    }

    @Test
    void CanSaveOneStudentToDatabaseAndReturnStudentWithId() {
        Student savedStudent = manager.save(student1);

        assertThat(savedStudent.getId()).isNotNull();

        output(createdTable).toConsole();
    }

    @Test
    void CanSaveTwoStudentsToDatabaseAndReturnStudentsWithId() {
        Student savedStudent = manager.save(student1);
        Student savedBeavis = manager.save(new Student("Beavis"));

        assertThat(savedStudent.getId()).isPositive();
        assertThat(savedBeavis.getId()).isGreaterThan(savedStudent.getId());

        output(createdTable).toConsole();
    }

    @Test
    void WhenSavingExistingObjectIntoDatabaseThenReturnTheSameAndDontSaveIt() {
        manager.save(student1);
        manager.save(student1);
        manager.save(student1);

        assertThat(createdTable).hasNumberOfRows(1);

        output(createdTable).toConsole();
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

        output(table).toConsole();
    }

}