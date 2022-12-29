package org.example.persistence.utilities;

import org.example.persistence.annotations.Column;
import org.example.persistence.annotations.Entity;
import org.example.persistence.annotations.Id;
import org.example.persistence.annotations.Table;
import org.example.persistence.sql.SQLDialect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class AnnotationUtils {

    private AnnotationUtils() {
    }

    public static boolean entityAnnotationIsPresent(Class<?> clss) {
        return clss.isAnnotationPresent(Entity.class);
    }

    public static String getTableName(Class<?> clss) {
        if (clss.isAnnotationPresent(Table.class)) {
            String tableName = clss.getAnnotation(Table.class).name();
            return tableName.equals("") ? (clss.getSimpleName() + "s") : tableName;
        } else {
            return clss.getSimpleName() + "s";
        }
    }

    public static List<String> declareColumnNamesFromEntityFields(Class<?> clss) {
        List<String> columnNames = new ArrayList<>();
        for (Field declaredField : clss.getDeclaredFields()) {
            String fieldTypeName = declaredField.getType().getSimpleName();
            String columnName = getColumnName(declaredField);
            String constraints =
                    (isUnique(declaredField) ? " UNIQUE " : "") +
                            (canBeNull(declaredField) ? "" : " NOT NULL");
            String idTag = sqlIdStatement(declaredField);
            switch (fieldTypeName) {
                case "String" -> columnNames.add(columnName + SQLDialect.STRING + idTag + constraints);
                case "Long", "long" -> columnNames.add(columnName + SQLDialect.LONG + idTag + constraints);
                case "LocalDate" -> columnNames.add(columnName + SQLDialect.DATETIME + idTag + constraints);
                case "Boolean", "boolean" -> columnNames.add(columnName + SQLDialect.BOOLEAN + idTag + constraints);
                case "int", "Integer" -> columnNames.add(columnName + SQLDialect.INT + idTag + constraints);
                default -> columnNames.add("");
            }
        }
        return columnNames;
    }

    public static String getColumnName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            String fieldName = field.getAnnotation(Column.class).name();
            return fieldName.equals("") ? field.getName() : fieldName;
        } else {
            return field.getName();
        }
    }

    public static boolean isUnique(Field field) {
        return field.getAnnotation(Column.class).unique();
    }

    public static boolean canBeNull(Field field) {
        return field.getAnnotation(Column.class).nullable();
    }

    private static String sqlIdStatement(Field declaredField) {
        return declaredField.isAnnotationPresent(Id.class) ? SQLDialect.ID : "";
    }

    public static String getIdFieldName(Class<?> clss) {
        String result = "";
        for (Field field : clss.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                result = field.getName();
                break;
            }
        }
        return result;
    }
}