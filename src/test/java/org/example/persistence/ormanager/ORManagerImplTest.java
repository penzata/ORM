package org.example.persistence.ormanager;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.assertj.db.type.Table;
import org.example.domain.model.Academy;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.db.api.Assertions.assertThat;
import static org.assertj.db.output.Outputs.output;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class ORManagerImplTest {
    ORManager manager;
    HikariDataSource dataSource;
    Connection connection;
    PreparedStatement pstmt;
    Table createdStudentsTable;
    Table createdAcademiesTable;
    Student student1;

    @AfterEach
    void tearDown() throws SQLException {
        pstmt = connection.prepareStatement("DROP TABLE IF EXISTS students");
        pstmt.executeUpdate();
        pstmt = connection.prepareStatement("DROP TABLE IF EXISTS academies");
        pstmt.executeUpdate();

        if (connection != null) {
            connection.close();
        }
        if (dataSource != null) {
            dataSource.close();
        }
        if (pstmt != null) {
            pstmt.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test");
        manager = Utils.withDataSource(dataSource);
        manager.register(Academy.class, Student.class);
        connection = dataSource.getConnection();
        createdStudentsTable = new Table(dataSource, "students");
        createdAcademiesTable = new Table(dataSource, "academies");
        student1 = new Student("Bob", "", 66, LocalDate.now());
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
        Student savedSecondStudent = manager.save(new Student("Dale", "", 66, LocalDate.now()));

        assertThat(savedStudent.getId()).isPositive();
        assertThat(savedSecondStudent.getId()).isGreaterThan(savedStudent.getId());

        output(createdStudentsTable).toFile("tableFromTest");
    }

    @Test
    void WhenSavingExistingObjectIntoDatabaseThenReturnTheSameAndDontSaveIt() {
        Student st = new Student("Shelly", "", 66, LocalDate.now());
        manager.save(st);
        manager.save(st);
        manager.save(st);

        assertThat(createdStudentsTable).hasNumberOfRows(1);

        output(createdStudentsTable).toFile("tableFromTest");
    }

    @Test
    void canFindPersonById() {
        Student savedStudent = manager.save(new Student("Harry", "", 66, LocalDate.now()));

        Optional<Student> foundStudent = manager.findById(savedStudent.getId(), Student.class);

        assertThat(foundStudent).contains(savedStudent);
    }

    @Test
    void WhenIdDoesntExistsThenReturnEmptyOptional() {
        Optional<Student> personToBeFound = manager.findById(-1L, Student.class);

        assertThat(personToBeFound).isNotPresent();
    }

    @Test
    void WhenFindAllThenReturnAllSavedToDBObjects() {
        manager.save(new Student("Ivan", "", 66, LocalDate.now()));
        manager.save(new Student("Petkan", "", 66, LocalDate.now()));

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
    void WhenSavingThreeEntitiesTwoDBThenReturnRecordsCountToBeEqualToThree() {
        long startCount = manager.recordsCount(Student.class);
        Student un = manager.save(new Student("Un", "", 66, LocalDate.now()));
        Student dos = manager.save(new Student("Dos", "", 66, LocalDate.now()));
        Student tres = manager.save(new Student("Tres", "", 66, LocalDate.now()));

        long endCount = manager.recordsCount(Student.class);

        assertThat(endCount).isEqualTo(startCount + 3);
    }

    @Test
    void WhenDeletingFromRecordsThenReturnRecordsCountWithOneRecordLess() {
        Student savedStudent = manager.save(new Student("Laura", "", 66, LocalDate.now()));
        long startCount = manager.recordsCount(Student.class);

        manager.delete(savedStudent);
        long endCount = manager.recordsCount(Student.class);

        assertThat(endCount).isEqualTo(startCount - 1);
    }

    @Test
    void WhenDeletingRecordThenReturnTrue() {
        manager.save(student1);

        boolean result = manager.delete(student1);

        assertTrue(result);
    }

    @Test
    void WhenDeletingRecordThatDoesntExistsThenReturnFalse() {
        Student notSavedInDBStudent = new Student("Andi", "", 66, LocalDate.now());

        boolean result = manager.delete(notSavedInDBStudent);

        assertFalse(result);
    }

    @Test
    void WhenDeletingRecordThatDoesntExistsThenDontThrowException() {
        Student notSavedInDBStudent = new Student("Andi", "", 66, LocalDate.now());

        assertDoesNotThrow(() -> manager.delete(notSavedInDBStudent));
    }

    @Test
    void WhenDeletingRecordSetAutoGeneratedIdToNull() {
        Student savedStudent = manager.save(new Student("Bobby", "", 66, LocalDate.now()));

        manager.delete(savedStudent);

        assertThat(savedStudent.getId()).isNull();
    }

    @Test
    void canDeleteMultipleRecords() {
        Student catherine = manager.save(new Student("Catherine", "", 66, LocalDate.now()));
        Student audrey = manager.save(new Student("Audrey", "", 66, LocalDate.now()));
        long startCount = manager.recordsCount(Student.class);

        manager.delete(catherine, audrey);
        long endCount = manager.recordsCount(Student.class);

        assertThat(endCount).isLessThanOrEqualTo(startCount - 2);
    }

    @Test
    void WhenUpdatingRecordAndFindItByIdThenReturnTheUpdatedRecord() {
        Student savedStudent = manager.save(new Student("Donna", "", 66, LocalDate.now()));
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

        output(createdStudentsTable).toFile("tableFromTest");
    }

    @Test
    void canUpdateRecord() {
        Student savedStudent = manager.save(new Student("Donna", "", 66, LocalDate.now()));
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

        output(createdStudentsTable).toFile("tableFromTest");
    }

    @Test
    void WhenUpdatingRecordInsideDBThenRefreshReturnsUpdatedRecord() throws SQLException {
        Student student = new Student("John", "Doe", 51, null);
        manager.save(student);
        String updateStatement = """
                UPDATE students 
                SET second_name = 'Travolta'
                        WHERE id = 1
                """;

        connection.prepareStatement(updateStatement).executeUpdate();
        Student refreshedStudent = manager.refresh(student);

        assertEquals("Bonanza", student.getSecondName());
    }

    @Test
    void WhenInsertingIntoDBThenFindAllReturnsInsertedRecords() {
        String insertStatement = """
                INSERT INTO students
                VALUES (1, 'John', 'Doe', 51, null, null)
                """;
    }

}