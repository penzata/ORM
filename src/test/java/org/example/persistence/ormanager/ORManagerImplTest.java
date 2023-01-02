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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        student1 = new Student("Bob");
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
        Student savedBeavis = manager.save(new Student("Dale"));

        assertThat(savedStudent.getId()).isPositive();
        assertThat(savedBeavis.getId()).isGreaterThan(savedStudent.getId());

        output(createdStudentsTable).toFile("tableFromTest");
    }

    @Test
    void WhenSavingExistingObjectIntoDatabaseThenReturnTheSameAndDontSaveIt() {
        Student st = new Student("Shelly");
        manager.save(st);
        manager.save(st);
        manager.save(st);

        assertThat(createdStudentsTable).hasNumberOfRows(1);

        output(createdStudentsTable).toFile("tableFromTest");
    }

    @Test
    void canFindPersonById() {
        Student savedStudent = manager.save(new Student("Harry"));

        Optional<Student> foundStudent = manager.findById(savedStudent.getId(), Student.class);

        assertThat(foundStudent).contains(savedStudent);
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
    void WhenDeletingFromRecordsThenReturnRecordsCountWithOneRecordLess() {
        Student savedStudent = manager.save(new Student("Laura"));
        int startCount = manager.recordsCount(Student.class);

        manager.delete(savedStudent);
        int endCount = manager.recordsCount(Student.class);

        assertThat(endCount).isEqualTo(startCount - 1);
    }

    @Test
    void WhenDeletingRecordThenReturnTrue() {
        boolean result = manager.delete(student1);

        assertTrue(result);
    }

    @Test
    void WhenDeletingRecordThatDoesntExistsThenReturnFalse() {
        Student notSavedInDBStudent = new Student("Andi");

        boolean result = manager.delete(notSavedInDBStudent);

        assertFalse(result);
    }

    @Test
    void WhenDeletingRecordSetAutoGeneratedIdToNull() {
        Student savedStudent = manager.save(new Student("Bobby"));

        manager.delete(savedStudent);

        assertThat(savedStudent.getId()).isNull();
    }

    @Test
    void canDeleteMultipleRecords() {
        Student catherine = manager.save(new Student("Catherine"));
        Student audrey = manager.save(new Student("Audrey"));
        int startCount = manager.recordsCount(Student.class);

        manager.delete(catherine, audrey);
        int endCount = manager.recordsCount(Student.class);

        assertThat(endCount).isLessThanOrEqualTo(startCount - 2);
    }

    @Test
    void WhenUpdatingRecordAndFindItByIdThenReturnTheUpdatedRecord() {
        Student savedStudent = manager.save(new Student("Donna"));
        Student foundStudent = manager.findById(savedStudent.getId(), Student.class).get();

        foundStudent.setFirstName("Don");
        manager.update(foundStudent);
        Student foundUpdatedStudent = manager.findById(foundStudent.getId(), Student.class).get();

        assertThat(foundUpdatedStudent.getFirstName()).isEqualTo(foundStudent.getFirstName());
        assertThat(foundUpdatedStudent).usingRecursiveComparison().isEqualTo(foundStudent);

        assertThat(createdStudentsTable).column(0)
                .value().isEqualTo(foundStudent.getId())
                .column(1)
                .value().isEqualTo("Don");
    }

    @Test
    void canUpdateRecord() {
        Student savedStudent = manager.save(new Student("Donna"));
        Student returnedStudent = manager.findById(savedStudent.getId(), Student.class).get();

        savedStudent.setFirstName("Dina");
        manager.update(savedStudent);
        Student foundStudent = manager.findById(savedStudent.getId(), Student.class).get();

        assertThat(foundStudent.getFirstName()).isNotEqualTo(returnedStudent.getFirstName());
        assertThat(foundStudent).usingRecursiveComparison().isNotEqualTo(returnedStudent);

        assertThat(createdStudentsTable).column(0)
                .value().isEqualTo(savedStudent.getId())
                .column(1)
                .value().isEqualTo("Dina");
    }

}