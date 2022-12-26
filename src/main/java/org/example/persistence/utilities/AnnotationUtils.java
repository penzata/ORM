package org.example.persistence.utilities;

import org.example.persistence.annotations.Column;
import org.example.persistence.annotations.Entity;
import org.example.persistence.annotations.Table;
import org.example.persistence.sql.SQLDialect;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;

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

    public static String getFieldName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            String fieldName = field.getAnnotation(Column.class).name();
            return fieldName.equals("") ? field.getName() : fieldName;
        } else {
            return field.getName();
        }
    }

    public static FieldInfo getIdField(Class<?> clss) {

        return null;
    }

    public static boolean isUnique(Field field) {
        return field.getAnnotation(Column.class).unique();
    }

    public static boolean canBeNull(Field field) {
        return field.getAnnotation(Column.class).nullable();
    }

    public static void setColumnName(ArrayList<String> sql, Class<?> type, String name, boolean isUnique, boolean canBeNull) {
        String constraints =
                (isUnique ? " UNIQUE " : "") +
                        (canBeNull ? "" : "NOT NULL");

        if (type == Long.class) {
            sql.add(name + SQLDialect.ID);
        }
        if (type == String.class) {
            sql.add(name + SQLDialect.NAME + constraints);
        } else if (type == LocalDate.class) {
            sql.add(name + SQLDialect.DATETIME + constraints);
        } else if (type == int.class) {
            sql.add(name + SQLDialect.INT + constraints);
        } else if (type == boolean.class) {
            sql.add(name + SQLDialect.BOOLEAN + constraints);
        }
    }
}
