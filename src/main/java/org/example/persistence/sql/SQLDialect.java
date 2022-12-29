package org.example.persistence.sql;

import org.example.persistence.utilities.AnnotationUtils;

public class SQLDialect {
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS";
    public static final String SQL_INSERT_STUDENT = """
            INSERT INTO STUDENTS (first_name) values(?);
            """;
    public static final String SQL_FIND_BY_ID_STUDENT = """
            SELECT * FROM STUDENTS WHERE id=?
            """;
    public static final String SQL_FIND_ALL = """
            SELECT * FROM
            """;

    public static final String ID = " GENERATED ALWAYS AS IDENTITY PRIMARY KEY";
    public static final String STRING = " VARCHAR(255)";
    public static final String DATETIME = " DATETIME";
    public static final String INT = " INT";
    public static final String LONG = " BIGINT";
    public static final String BOOLEAN = " BOOLEAN";

    public static String getTableNameForInsert(Class<?> clss) {
        String tableName = AnnotationUtils.getTableName(clss);
        return String.format("INSERT INTO %s (first_name) values(?)", tableName);
    }

    public static String getTableNameForSelect(Class<?> clss) {
        String tableName = AnnotationUtils.getTableName(clss);
        return String.format("SELECT * FROM %s WHERE id=?", tableName);
    }
}
