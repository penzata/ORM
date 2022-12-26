package org.example.persistence.ormanager;

import com.zaxxer.hikari.HikariDataSource;
import org.assertj.db.type.Table;
import org.example.domain.model.Student;
import org.example.persistence.utilities.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.db.api.Assertions.assertThat;
import static org.assertj.db.output.Outputs.output;

class ORManagerImplTest {
    //language=H2
    private static final String STUDENTS_TABLE = """
            CREATE TABLE IF NOT EXISTS students
            (
            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
            first_name VARCHAR(30) NOT NULL
            )
            """;
    static ORManager manager;
    static HikariDataSource dataSource;
    static Connection connection;
    static PreparedStatement createTableStmt;
    Table createdTable;
    Student student1;

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
        if (createTableStmt != null) {
            createTableStmt.close();
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
        createTableStmt = connection.prepareStatement(STUDENTS_TABLE);
        createTableStmt.execute();

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

        assertThat(savedStudent.getId()).isGreaterThan(0);
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

}