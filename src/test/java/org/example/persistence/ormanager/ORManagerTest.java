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
    private static final String DATABASE_PATH = "h2.properties";
    ORManager ormManager;
    HikariDataSource dataSource;
    Connection connection;
    PreparedStatement ps;
    Source source;
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
        ormManager = Utils.withPropertiesFrom(DATABASE_PATH);
        connection = Utils.getConnection();
        log.atDebug().log("Is the connection valid: {}", connection.isValid(1000));
        assert connection != null;
        connection.setAutoCommit(false);

        ormManager.register(Academy.class, Student.class);

        source = new Source("jdbc:h2:file:./src/database/testDB", "", "");

        createdStudentsTable = new Table(source, "students");
        createdAcademiesTable = new Table(source, "academies");
    }

    //todo rework the test
    @Test
    void stuff() {
        Student st1 = new Student("Don", "Johnson", 63, LocalDate.now());
        Student st2 = new Student("Emma", "Thompson", 56, LocalDate.now());
        Student st3 = new Student("Kurt", "Russell", 44, LocalDate.now());

        Academy ac1 = new Academy("SoftServe");
        ormManager.save(ac1);
        Academy ac2 = new Academy("Khan");
        ormManager.save(ac2);

        st1.setAcademy(ac1);
        log.atError().log("{}", st1);
        st2.setAcademy(ac1);
        st3.setAcademy(ac2);

        ormManager.save(st1);
        ormManager.save(st2);
        ormManager.save(st3);

        output(createdStudentsTable).toConsole();
        output(createdAcademiesTable).toConsole();

        assertThat(createdStudentsTable).hasNumberOfRows(3);
        assertThat(createdAcademiesTable).hasNumberOfRows(2);
    }

}