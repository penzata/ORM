package org.example.persistence.utilities;

import lombok.extern.slf4j.Slf4j;
import org.assertj.db.type.Source;
import org.assertj.db.type.Table;
import org.assertj.db.type.ValueType;
import org.example.domain.model.Student;
import org.example.persistence.ormanager.ORManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.db.api.Assertions.assertThat;
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
    PreparedStatement ps;
    Source source;
    Table table;
    ORManager ormManager;

    @BeforeEach
    void setUp() throws SQLException {
        ormManager = Utils.withPropertiesFrom(DATABASE_PATH);
        conn = Utils.getConnection();
        log.atDebug().log("is the connection valid: {}", conn.isValid(1000));
        conn.prepareStatement(TESTS_TABLE).execute();
        source = new Source("jdbc:h2:file:./src/database/testDB", "", "");
        table = new Table(source, "tests");
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
    void withPropertiesFromCheck() throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(SQL_ADD_ONE)) {
            Student st1 = new Student("Bob");
            Student st2 = new Student("Ani");
            Student st3 = new Student("Dale");
            Student st4 = new Student("Laura");
            Student st5 = new Student("Shelly");

            stmt.setString(1, st1.getFirstName());
            stmt.executeUpdate();
            stmt.setString(1, st2.getFirstName());
            stmt.executeUpdate();
            stmt.setString(1, st3.getFirstName());
            stmt.executeUpdate();
            stmt.setString(1, st4.getFirstName());
            stmt.executeUpdate();
            stmt.setString(1, st5.getFirstName());
            stmt.executeUpdate();

            assertThat(table).row(0)
                    .value().isEqualTo(1L)
                    .value().isEqualTo("Bob");

            assertThat(table).column("first_name").isOfType(ValueType.TEXT, true);
        }
        }
    }