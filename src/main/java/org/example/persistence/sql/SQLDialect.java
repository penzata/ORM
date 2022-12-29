package org.example.persistence.sql;

import org.example.persistence.annotations.Id;
import org.example.persistence.utilities.AnnotationUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SQLDialect {
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS";
    public static final String SQL_FIND_ALL = """
            SELECT * FROM
            """;

    public static final String ID = " GENERATED ALWAYS AS IDENTITY PRIMARY KEY";
    public static final String STRING = " VARCHAR(255)";
    public static final String DATETIME = " DATETIME";
    public static final String INT = " INT";
    public static final String LONG = " BIGINT";
    public static final String BOOLEAN = " BOOLEAN";

    public static String getTableAndColumnNamesForInsert(Class<?> clss) {
        Field[] declaredFields = clss.getDeclaredFields();
        List<String> columnNames = new ArrayList<>();
        List<String> parameters = new ArrayList<>();
        for (Field declaredField : declaredFields) {
           if(!declaredField.isAnnotationPresent(Id.class)) {
               columnNames.add(AnnotationUtils.getColumnName(declaredField));
               parameters.add("?");
           }
        }
        String tableName = AnnotationUtils.getTableName(clss);

        return String.format("INSERT INTO %s (%s) values(%s)"
                , tableName, String.join(", ", columnNames)
        , String.join(", ", parameters));
    }

    public static String getTableNameForSelect(Class<?> clss) {
        String tableName = AnnotationUtils.getTableName(clss);
        return String.format("SELECT * FROM %s WHERE id=?", tableName);
    }
}
