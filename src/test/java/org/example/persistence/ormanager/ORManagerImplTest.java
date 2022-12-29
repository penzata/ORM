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
    Table createdTable;
    Student student1;

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test");
        manager = Utils.withDataSource(dataSource);
        connection = dataSource.getConnection();
        manager.register(Student.class);

        student1 = new Student("Johny");
        createdTable = new Table(dataSource, "students");
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
        @org.example.persistence.annotations.Table(name = "named_table")
        class WithAnno {
            @Id
            @Column(name = "trialId")
            int trialId;
            @Column(name = "trialFirstName", nullable = false)
            String trialFirstName;
            @Column
            boolean under18;
        }
        Table table = new Table(dataSource, "named_table");

        manager.register(WithAnno.class);

        assertThat(table).column("trialId")
                .isOfType(ValueType.TEXT, true);
    }

}