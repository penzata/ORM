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

    public static String getTableName(Class<?> cls) {
        Table annotation = cls.getAnnotation(Table.class);
        if (annotation != null) {
            String name = annotation.name();
            if (!name.equals("")) {
                return name;
            }
        }
        return cls.getSimpleName();
    }

    public static String getFieldName(Field field) {
        String name = field.getAnnotation(Column.class).name();
        if (name.equals("")) {
            name = field.getName();
        }
        return name;
    }

    public static boolean isUnique(Field field) {
        return field.getAnnotation(Column.class).unique();
    }

    public static boolean canBeNull(Field field) {
        return field.getAnnotation(Column.class).nullable();
    }
}
