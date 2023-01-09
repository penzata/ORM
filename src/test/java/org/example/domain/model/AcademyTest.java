package org.example.domain.model;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.assertj.db.type.Table;
import org.example.persistence.ormanager.ORManager;
import org.example.persistence.utilities.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.assertj.db.output.Outputs.output;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class AcademyTest {
    ORManager manager;
    HikariDataSource dataSource;
    Connection connection;
    PreparedStatement pstmt;
    Table createdStudentsTable;
    Table createdAcademiesTable;
    Student st1;
    Student st2;
    Student st3;
    Academy ac;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test");
        manager = Utils.withDataSource(dataSource);
        manager.register(Academy.class, Student.class);
        connection = dataSource.getConnection();
        log.atDebug().log("Is the connection valid: {}", connection.isValid(1000));
        createdStudentsTable = new Table(dataSource, "students");
        createdAcademiesTable = new Table(dataSource, "academies");

        st1 = new Student("Ed", "", 45, LocalDate.now());
        st2 = new Student("Edd", "", 34, LocalDate.now());
        st3 = new Student("Eddy", "", 45, LocalDate.now());
        ac = new Academy("SoftServe");
    }

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

    @Test
    void WhenAddingStudentsToAcademyThenGetStudentsReturnsCorrectList() {
        st1.setAcademy(ac);
        st2.setAcademy(ac);
        st3.setAcademy(ac);

        manager.save(ac);
        manager.save(st1);
        manager.save(st2);
        manager.save(st3);

        assertEquals(3, ac.getStudents().size());
        log.atDebug().log("{}", ac.getStudents());

        output(createdStudentsTable).toConsole();
        output(createdAcademiesTable).toConsole();
    }

    @Test
    void WhenUsingSetMethodThenGetMethodReturnsCorrectValue() {
        ac.setName("Khan");
        ac.setId(5L);

        assertEquals("Khan", ac.getName());
        assertEquals(5, ac.getId());
    }

}