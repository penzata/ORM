package org.example.persistence.utilities;

import org.example.persistence.annotations.Column;
import org.example.persistence.annotations.Entity;
import org.example.persistence.annotations.Id;
import org.example.persistence.annotations.Table;
import org.example.persistence.sql.SQLDialect;

import java.lang.reflect.Field;
import java.time.LocalDate;
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

    public static String getColumnName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            String fieldName = field.getAnnotation(Column.class).name();
            return fieldName.equals("") ? field.getName() : fieldName;
        } else {
            return field.getName();
        }
    }

    public static String getIdField(Class<?> clss) {
        String result = "";
        for (Field field : clss.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                result = field.getName();
                break;
            }
        }
        return result;
    }

    public static boolean isUnique(Field field) {
        return field.getAnnotation(Column.class).unique();
    }

    public static boolean canBeNull(Field field) {
        return field.getAnnotation(Column.class).nullable();
    }

    public static void sqlColumnDeclaration(List<String> columnNames, Class<?> fieldType, String name, boolean isUnique, boolean canBeNull) {
        String constraints =
                (isUnique ? " UNIQUE " : "") +
                        (canBeNull ? "" : " NOT NULL");

        if (fieldType == Long.class) {
            columnNames.add(name + SQLDialect.ID);
        }
        if (fieldType == String.class) {
            columnNames.add(name + SQLDialect.STRING + constraints);
        } else if (fieldType == LocalDate.class) {
            columnNames.add(name + SQLDialect.DATETIME + constraints);
        } else if (fieldType == int.class) {
            columnNames.add(name + SQLDialect.INT + constraints);
        } else if (fieldType == boolean.class) {
            columnNames.add(name + SQLDialect.BOOLEAN + constraints);
        }
    }

}
