package org.example.persistence.ormanager;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.assertj.db.type.DateValue;
import org.assertj.db.type.Table;
import org.example.domain.model.Academy;
import org.example.domain.model.Student;
import org.example.exceptionhandler.EntityAnnotationNotFoundException;
import org.example.exceptionhandler.EntityNotFoundException;
import org.example.exceptionhandler.IdAnnotationNotFoundException;
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
import java.time.Month;
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

        output(createdStudentsTable).toFile("tableFromTest.txt");
    }

    @Test
    void CanSaveTwoStudentsToDatabaseAndReturnStudentsWithId() {
        Student savedStudent = manager.save(student1);
        Student savedSecondStudent = manager.save(new Student("Dale", "", 66, LocalDate.now()));

        assertThat(savedStudent.getId()).isPositive();
        assertThat(savedSecondStudent.getId()).isGreaterThan(savedStudent.getId());

        output(createdStudentsTable).toFile("tableFromTest.txt");
    }

    @Test
    void WhenSavingToDBThenInsertCorrectValuesIntoTable() {
        manager.save(new Student("Don", "DeLio", 86, LocalDate.of(1989, Month.APRIL, 24)));
        manager.save(new Student("Kurt", "Vonnegut", 100, LocalDate.of(1995, Month.DECEMBER, 13)));

        assertThat(createdStudentsTable).row(0)
                .value().isEqualTo(1)
                .value().isEqualTo("Don")
                .value().isEqualTo("DeLio")
                .value().isEqualTo(86)
                .value().isEqualTo(DateValue.of(1989, 4, 24))
                .row(1)
                .value().isEqualTo(2)
                .value().isEqualTo("Kurt")
                .value().isEqualTo("Vonnegut")
                .value().isEqualTo(100)
                .value().isEqualTo(DateValue.of(1995, 12, 13));

        output(createdStudentsTable).toFile("tableFromTest.txt");
    }

    @Test
    void WhenSavingExistingIntoDatabaseObjectMultipleTimesThenTheRowsCountDoesntChange() {
        Student st = new Student("Shelly", "", 66, LocalDate.now());

        manager.save(st);
        manager.save(st);
        manager.save(st);

        assertThat(createdStudentsTable).hasNumberOfRows(1);

        output(createdStudentsTable).toFile("tableFromTest.txt");
    }

    @Test
    void WhenPersistObjectWithNullPropertyValuesThenRecordWontBeSavedToDbBecauseOfColumnConstraints() {
        Student studentWithNullFields = new Student(null, null, null, null);

        manager.save(studentWithNullFields);

        assertThat(createdStudentsTable).hasNumberOfRows(0);

        output(createdStudentsTable).toConsole();
    }

    @Test
    void WhenSavingObjectWithIdButOtherFieldsAreNullThenRecordWontBeUpdatedInDbBecauseOfColumnConstraints() {
        Student studentWithNullFields = new Student(null, null, null, null);
        studentWithNullFields.setId(1L);
        manager.save(student1);

        manager.save(studentWithNullFields);

        assertThat(createdStudentsTable).row(0)
                .value().isEqualTo(1)
                .value().isNotNull()
                .value().isNotNull()
                .value().isNotNull()
                .value().isNotNull()
                .value().isNull();

        output(createdStudentsTable).toConsole();
    }

    @Test
    void canFindById() {
        Student savedStudent = manager.save(new Student("Harry", "", 66, LocalDate.now()));

        Optional<Student> foundStudent = manager.findById(savedStudent.getId(), Student.class);

        assertThat(foundStudent).contains(savedStudent);
    }

    @Test
    void WhenTryToFindByIdAndIdExistsReturnObject() throws SQLException {
        String dbInsertedStudent = """
                INSERT INTO students (first_name, second_name, age, graduate_academy, academy_id)
                values ('Jimmy', 'Tulip', 51, null, null)
                """;
        connection.prepareStatement(dbInsertedStudent).executeUpdate();

        Student foundStudent = manager.findById(1, Student.class).get();

        assertThat(foundStudent.getId()).isEqualTo(1);
    }

    @Test
    void WhenIdDoesntExistsThenReturnEmptyOptional() {
        Optional<Student> studentToBeFound = manager.findById(-1L, Student.class);

        assertThat(studentToBeFound).isNotPresent();
    }

    @Test
    void WhenFindByIdGenericEntityThenReturnsTheCorrectEntityFromDB() {
        Table createdDudesTable = new Table(dataSource, "dudes");
        manager.register(Dude.class);
        Dude dude = new Dude("The Dude", true, 178.3, 1656189648);
        Dude savedDude = manager.save(dude);
        log.atDebug().log("{}", savedDude);

        Optional<Dude> foundDude = manager.findById(dude.getId(), Dude.class);
        boolean isAdult = foundDude.get().isAdult();

        assertThat(foundDude).contains(dude);
        assertThat(foundDude.get().getHeight()).isEqualTo(178.3);
        assertTrue(isAdult);

        output(createdDudesTable).toFile("tableFromTest.txt");
    }

    @Test
    void WhenFindAllThenReturnAllSavedToDBObjects() {
        manager.save(new Student("Ivan", "", 21, LocalDate.now()));
        manager.save(new Student("Petkan", "", 26, LocalDate.now()));
        manager.save(new Student("Petkan", "", 26, LocalDate.now()));
        manager.save(student1);
        manager.save(student1);

        List<Student> allStudents = manager.findAll(Student.class);

        assertThat(allStudents).hasSize(4);
    }

    @Test
    void WhenInsertingIntoDBThenFindAllReturnsCorrectRecordsCount() throws SQLException {
        String dbInsertedStudent = """
                INSERT INTO students (first_name, second_name, age, graduate_academy, academy_id)
                VALUES ('John', 'Doe', 51, null, null)
                """;
        connection.prepareStatement(dbInsertedStudent).executeUpdate();
        String dbInsertedSecondStudent = """
                INSERT INTO students (first_name, second_name, age, graduate_academy, academy_id)
                VALUES ('Jane', 'Doe', 32, '2018-04-27', null)
                """;
        connection.prepareStatement(dbInsertedSecondStudent).executeUpdate();

        int recordsCount = manager.findAll(Student.class).size();

        assertThat(recordsCount).isEqualTo(2);

        output(createdStudentsTable).toFile("tableFromTest.txt");
    }

    @Test
    void WhenRegisterAnEntityThenReturnATableWithColumnNamesMatchingItsFields() {
        Table createdDudesTable = new Table(dataSource, "dudes");

        manager.register(Dude.class);

        assertThat(createdDudesTable).hasNumberOfColumns(5);
        assertThat(createdDudesTable).column(0)
                .hasColumnName("id")
                .column(1)
                .hasColumnName("the_real_name")
                .column(2)
                .hasColumnName("adult")
                .column(3)
                .hasColumnName("height")
                .column(4)
                .hasColumnName("collection_of_cards");

        output(createdDudesTable).toFile("tableFromTest.txt");
    }

    @Test
    void WhenRegisterAndEntityAnnotationIsNotPresentThenThrowException() {
        class NoEntity {
            Integer id;
            String species;
        }

        assertThrows(EntityAnnotationNotFoundException.class, () -> manager.register(NoEntity.class));
    }

    @Test
    void WhenRegisterAndIdAnnotationIsNotPresentThenThrowException() {
        @Entity
        class NoId {
            Integer id;
            String species;
        }

        assertThrows(IdAnnotationNotFoundException.class, () -> manager.register(NoId.class));
    }

    @Test
    void WhenSavingThreeEntitiesToDBThenReturnRecordsCountToBeEqualToThree() {
        long startCount = manager.recordsCount(Student.class);

        Student un = manager.save(new Student("Un", "", 1, LocalDate.now()));
        Student dos = manager.save(new Student("Dos", "", 2, LocalDate.now()));
        Student tres = manager.save(new Student("Tres", "", 3, LocalDate.now()));
        long endCount = manager.recordsCount(Student.class);

        assertThat(endCount).isEqualTo(startCount + 3);
    }

    @Test
    void WhenDeletingThenReturnRecordsCountWithOneRecordLess() {
        Student savedStudent = manager.save(new Student("Laura", "", 68, LocalDate.now()));
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
        Student notSavedInDBStudent = new Student("Andi", "", 42, LocalDate.now());

        boolean result = manager.delete(notSavedInDBStudent);

        assertFalse(result);
    }

    @Test
    void WhenDeletingRecordThatDoesntExistsThenDontThrowException() {
        Student notSavedInDBStudent = new Student("Andi", "", 42, LocalDate.now());

        assertDoesNotThrow(() -> manager.delete(notSavedInDBStudent));
    }

    @Test
    void WhenDeletingRecordSetAutoGeneratedIdToNull() {
        Student savedStudent = manager.save(new Student("Bobby", "", 78, LocalDate.now()));

        manager.delete(savedStudent);

        assertThat(savedStudent.getId()).isNull();
    }

    @Test
    void canDeleteMultipleRecords() {
        Student catherine = manager.save(new Student("Catherine", "", 42, LocalDate.now()));
        Student audrey = manager.save(new Student("Audrey", "", 39, LocalDate.now()));
        long startCount = manager.recordsCount(Student.class);

        manager.delete(catherine, audrey);
        long endCount = manager.recordsCount(Student.class);

        assertThat(endCount).isLessThanOrEqualTo(startCount - 2);
    }

    @Test
    void canUpdateRecord() {
        Student student = manager.save(new Student("Donna", "", 66, LocalDate.now()));

        student.setFirstName("Dina");
        Student updatedStudent = manager.update(student);

        assertThat(updatedStudent).isEqualTo(student);
        assertThat(updatedStudent).usingRecursiveComparison().isEqualTo(student);
        assertThat(createdStudentsTable).column(0)
                .value().isEqualTo(updatedStudent.getId())
                .column(1)
                .value().isEqualTo("Dina");

        output(createdStudentsTable).toFile("tableFromTest.txt");
    }

    @Test
    void WhenUpdatingObjectThatDoesntExistInDBThenThrowException() {
        Student notSavedInDBStudent = new Student("Donna", "", 19, LocalDate.now());

        assertThrows(EntityNotFoundException.class, () -> manager.update(notSavedInDBStudent));
    }

    @Test
    void WhenUpdatingRecordInsideDBThenRefreshReturnsUpdatedEntity() throws SQLException {
        Student savedStudent = manager.save(new Student("John", "Doe", 51, null));
        String updatedOnlyInDbStudent = """
                UPDATE students
                SET second_name = 'Travolta', age = 67, graduate_academy = '2018-04-27'
                WHERE id = 1
                """;
        connection.prepareStatement(updatedOnlyInDbStudent).executeUpdate();

        Student foundByIdStudent = manager.findById(savedStudent.getId(), Student.class).get();

        Student refreshedStudent = manager.refresh(savedStudent);

        assertThat(foundByIdStudent).isEqualTo(refreshedStudent);
        assertThat(refreshedStudent).isEqualTo(savedStudent);
        assertThat(refreshedStudent).usingRecursiveComparison().isEqualTo(savedStudent);

        output(createdStudentsTable).toFile("tableFromTest.txt");
    }

    @Test
    void WhenRecordIsNotUpdatedInsideDBThenRefreshReturnsSameEntity() throws SQLException {
        Student student = new Student("John", "Doe", 51, null);
        student.setId(1L);
        String dbInsertedStudent = """
                INSERT INTO students (first_name, second_name, age, graduate_academy, academy_id)
                VALUES ('John', 'Doe', 51, null, null)
                """;
        connection.prepareStatement(dbInsertedStudent).executeUpdate();

        Student refreshedStudent = manager.refresh(student);

        assertThat(refreshedStudent).isEqualTo(student);
    }

    @Test
    void WhenRefreshThenReturnNotNull() throws SQLException {
        Student savedStudent = manager.save(student1);
        String dbUpdatedStudent = """
                UPDATE students
                SET second_name = 'Dilon', age = 67, graduate_academy = '2022-11-23'
                WHERE id = 1
                """;
        connection.prepareStatement(dbUpdatedStudent).executeUpdate();

        Student refreshedStudent = manager.refresh(savedStudent);

        assertThat(refreshedStudent).isNotNull();

        output(createdStudentsTable).toFile("tableFromTest.txt");
    }

    @Test
    void WhenRefreshThenReturnCorrectValues() {
        Table createdDudesTable = new Table(dataSource, "dudes");
        manager.register(Dude.class);
        Dude dude = new Dude("Dudeeee", false, 141.1, 777777);
        manager.save(dude);

        Dude refreshedDude = manager.refresh(dude);

        assertThat(refreshedDude.getHeight()).isGreaterThanOrEqualTo(141.1);
    }

    @Data
    @Entity
    static class Dude {
        @Id
        private Integer id;
        @Column(name = "the_real_name", nullable = false)
        private String name;
        private boolean adult;
        private Double height;
        @Column(name = "collection_of_cards")
        private long collection;


        public Dude(String name, boolean adult, Double height, long collection) {
            this.name = name;
            this.adult = adult;
            this.height = height;
            this.collection = collection;
        }

        Dude() {
        }
    }
}