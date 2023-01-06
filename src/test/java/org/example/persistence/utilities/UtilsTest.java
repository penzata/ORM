package org.example.persistence.utilities;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.model.Student;
import org.example.persistence.ormanager.ORManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class UtilsTest {
    private static final String DATABASE_PATH = "h2.properties";
    //language=H2
    private static final String TESTS_TABLE = """
            CREATE TABLE IF NOT EXISTS tests
            (
            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
            first_name VARCHAR(30) NOT NULL
            )
            """;
    //language=H2
    private static final String SQL_ADD_ONE = "INSERT INTO tests (first_name) VALUES(?)";
    Connection conn;
    PreparedStatement stmt;
    ORManager ormManager;

    @BeforeEach
    void setUp() throws SQLException {
        ormManager = Utils.withPropertiesFrom(DATABASE_PATH);
        conn = Utils.getConnection();
        conn.setAutoCommit(false);
        log.atDebug().log("is the connection valid: {}", conn.isValid(1000));
        conn.prepareStatement(TESTS_TABLE).execute();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (conn != null) {
            conn.close();
        }
        if (stmt != null) {
            stmt.close();
        }
    }

    @Test
    void withPropertiesFrom() throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SQL_ADD_ONE, Statement.RETURN_GENERATED_KEYS)) {
            Student st1 = new Student("Dick", "Cheney", 81, LocalDate.now());

            stmt.setString(1, st1.getFirstName());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            while (rs.next()) {
                st1.setId(rs.getLong(1));
            }

            assertThat(st1.getId()).isPositive();
        }
    }
}