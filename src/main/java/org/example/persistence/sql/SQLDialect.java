package org.example.persistence.sql;

public class SQLDialect {
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS";
    public static final String SQL_INSERT_STUDENT = """
            INSERT INTO STUDENTS (first_name) values(?);
            """;

    public static final String ID = " BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY";
    public static final String NAME = " VARCHAR(255) ";
    public static final String DATETIME = " DATETIME ";
    public static final String INT = " INT ";
    public static final String BOOLEAN = " BOOLEAN ";
}
