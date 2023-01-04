package org.example.persistence.utilities;

import org.example.persistence.annotations.*;
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
        String tableName = clss.getSimpleName().toLowerCase() + "s";
        if (clss.isAnnotationPresent(Table.class)) {
            String annotatedTableName = clss.getAnnotation(Table.class).name();
            return annotatedTableName.equals("") ? tableName : annotatedTableName;
        } else {
            return tableName;
        }
    }

    public static List<String> declareColumnNamesFromEntityFields(Class<?> clss) {
        List<String> columnNames = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        for (Field declaredField : clss.getDeclaredFields()) {
            String fieldTypeName = declaredField.getType().getSimpleName();
            String columnName = getColumnName(declaredField);
            String idAndPKTag = autoincrementPrimaryKeyTag(declaredField);
            String constraints;
          
            if (declaredField.isAnnotationPresent(ManyToOne.class)) {
                fieldTypeName = "foreign key";
                columnName = declaredField.getAnnotation(ManyToOne.class).name();
                constraints = (canBeNullForManyToOne(declaredField) ? "" : " NOT NULL");
                String referenceTableName = declaredField.getType().getAnnotation(Table.class).name();
                keys.add("FOREIGN KEY(" + columnName + ") REFERENCES " + referenceTableName + "(id)");
            } else {
                constraints =
                        (isUnique(declaredField) ? " UNIQUE " : "") +
                        (canBeNull(declaredField) ? "" : " NOT NULL");
            }
            switch (fieldTypeName) {
                case "String" -> columnNames.add(columnName + SQLDialect.STRING + idAndPKTag + constraints);
                case "Long", "long", "foreign key" ->
                        columnNames.add(columnName + SQLDialect.LONG + idAndPKTag + constraints);
                case "LocalDate" -> columnNames.add(columnName + SQLDialect.LOCAL_DATE + idAndPKTag + constraints);
                case "Boolean", "boolean" -> columnNames.add(columnName + SQLDialect.BOOLEAN + idAndPKTag + constraints);
                case "int", "Integer" -> columnNames.add(columnName + SQLDialect.INTEGER + idAndPKTag + constraints);
                default -> columnNames.add("");
            }
            columnNames.addAll(keys);
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

    public static String getColumnNameFromManyToOne(Field field) {
        return field.getAnnotation(ManyToOne.class).name();
    }

    public static boolean isUnique(Field field) {
        return field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class).unique();
    }

    public static boolean canBeNull(Field field) {
        return field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class).nullable();
    }

    public static boolean canBeNullForManyToOne(Field field) {
        return field.getAnnotation(ManyToOne.class).nullable();
    }

    private static String autoincrementPrimaryKeyTag(Field declaredField) {
        String columnDefinition = "";
        if (declaredField.isAnnotationPresent(Column.class)) {
            columnDefinition = declaredField.getAnnotation(Column.class).columnDefinition();
        }

        String autoincrementTag = columnDefinition.equals("serial") ?
                SQLDialect.AUTO_INCREMENT_POSTGRE + SQLDialect.PRIMARY_KEY :
                SQLDialect.AUTO_INCREMENT_H2 + SQLDialect.PRIMARY_KEY;

        return declaredField.isAnnotationPresent(Id.class) ? autoincrementTag : "";
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