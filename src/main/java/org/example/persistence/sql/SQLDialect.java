package org.example.persistence.sql;

import org.example.persistence.annotations.Id;
import org.example.persistence.utilities.AnnotationUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SQLDialect {
    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS";
    public static final String SQL_FIND_ALL = """
            SELECT * FROM
            """;
    public static final String SQL_COUNT_ALL = """
            SELECT COUNT(*) FROM\040
            """;

    public static final String PRIMARY_KEY = " PRIMARY KEY";
    public static final String STRING = " VARCHAR(255)";
    public static final String LOCAL_DATE = " DATE";
    public static final String INTEGER = " INT";
    public static final String LONG = " BIGINT";
    public static final String DOUBLE = " DOUBLE PRECISION";
    public static final String BOOLEAN = " BOOLEAN";
    public static final String AUTO_INCREMENT_H2 = " GENERATED ALWAYS AS IDENTITY";
    public static final String AUTO_INCREMENT_POSTGRE = " SERIAL";

    private SQLDialect() {
    }
    public static String getTableForDelete(Class<?> cls){
        String tableName = AnnotationUtils.getTableName(cls);
        return String.format("DELETE FROM %s WHERE id = ?", tableName);
    }

    public static String sqlInsertStatement(Class<?> clss) {
        Field[] declaredFields = clss.getDeclaredFields();
        List<String> columnNames = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();
        for (Field declaredField : declaredFields) {
            if (!declaredField.isAnnotationPresent(Id.class)) {
                columnNames.add(AnnotationUtils.getColumnName(declaredField));
                placeholders.add("?");
            }
        }
        String tableName = AnnotationUtils.getTableName(clss);

        return String.format("INSERT INTO %s (%s) values(%s)"
                , tableName, String.join(", ", columnNames)
                , String.join(", ", placeholders));
    }

    public static String sqlSelectStatement(Class<?> clss) {
        String tableName = AnnotationUtils.getTableName(clss);
        return String.format("SELECT * FROM %s WHERE id=?", tableName);
    }

    public static String sqlUpdateStatement(Class<?> cls) {
        Field[] declaredFields = cls.getDeclaredFields();
        List<String> columnNamesAndPlaceholders = new ArrayList<>();
        String placeholder = " = ?";
        for (Field declaredField : declaredFields) {
            if (!declaredField.isAnnotationPresent(Id.class)) {
                columnNamesAndPlaceholders.add(AnnotationUtils.getColumnName(declaredField) + placeholder);
            }
        }
        String tableName = AnnotationUtils.getTableName(cls);

        return String.format("UPDATE %s SET %s WHERE id = ?",
                tableName, String.join(", ", columnNamesAndPlaceholders));
    }
}