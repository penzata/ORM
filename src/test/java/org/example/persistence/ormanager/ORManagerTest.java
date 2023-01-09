package org.example.persistence.ormanager;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.assertj.db.type.Source;
import org.assertj.db.type.Table;
import org.example.domain.model.Academy;
import org.example.domain.model.Student;
import org.example.persistence.utilities.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.assertj.db.api.Assertions.assertThat;
import static org.assertj.db.output.Outputs.output;

@Slf4j
class ORManagerTest {
    ORManager manager;
    HikariDataSource dataSource;
    Connection connection;
    PreparedStatement ps;
    Table createdStudentsTable;
    Table createdAcademiesTable;

    @AfterEach
    void tearDown() throws SQLException {
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

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test");
        manager = Utils.withDataSource(dataSource);
        manager.register(Academy.class, Student.class);
        connection = dataSource.getConnection();
        createdStudentsTable = new Table(dataSource, "students");
        createdAcademiesTable = new Table(dataSource, "academies");

        Student st5 = new Student("Kurt", "Russell", 44, LocalDate.now());
    }

    //todo rework the test & check for the thrown error
    @Test
    void stuff() {
        Student st1 = new Student("Don", "Johnson", 63, LocalDate.now());
        Student st2 = new Student("Emma", "Thompson", 56, LocalDate.now());
        Student st3 = new Student("Kurt", "Russell", 44, LocalDate.now());

        Academy ac1 = new Academy("SoftServe");
        Academy ac2 = new Academy("Khan");

        manager.save(ac1);
        manager.save(ac2);

        st1.setAcademy(ac1);
        log.atError().log("{}", st1);
        st2.setAcademy(ac1);
        st3.setAcademy(ac2);

        manager.save(st1);
        manager.save(st2);
        manager.save(st3);

        output(createdStudentsTable).toConsole();
        output(createdAcademiesTable).toConsole();

        assertThat(createdStudentsTable).hasNumberOfRows(3);
        assertThat(createdAcademiesTable).hasNumberOfRows(2);
    }

}