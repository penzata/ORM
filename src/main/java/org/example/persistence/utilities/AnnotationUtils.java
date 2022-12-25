package org.example.persistence.utilities;

import org.example.persistence.annotations.Column;
import org.example.persistence.annotations.Entity;
import org.example.persistence.annotations.Table;

import java.lang.reflect.Field;

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
        String fieldName = field.getAnnotation(Column.class).name();
        return fieldName.equals("") ? field.getName() : fieldName;
    }

    public static boolean isUnique(Field field) {
        return field.getAnnotation(Column.class).unique();
    }

    public static boolean canBeNull(Field field) {
        return field.getAnnotation(Column.class).nullable();
    }
}
