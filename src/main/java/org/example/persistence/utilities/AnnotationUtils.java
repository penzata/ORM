package org.example.persistence.utilities;

import org.example.exceptionhandler.IdAnnotationNotFoundException;
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

    /**
     * @param clss Class.
     * @return Collection of strings which contains the names of the columns (taken from the entity's fields)
     * needed for CREATE TABLE sql statement.
     */
    public static List<String> declareColumnNamesFromEntityFields(Class<?> clss) {
        List<String> columnNames = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        if (idAnnotationIsPresent(clss)) {
            for (Field declaredField : clss.getDeclaredFields()) {
                String fieldTypeName = declaredField.getType().getSimpleName();
                String columnName = getColumnName(declaredField);
                String idAndPKTag = autoincrementPrimaryKeyTag(declaredField);
                String constraints = (isUnique(declaredField) ? " UNIQUE " : "") +
                        (canBeNull(declaredField) ? "" : " NOT NULL");

                if (declaredField.isAnnotationPresent(ManyToOne.class)) {
                    fieldTypeName = "foreign key";
                    columnName = declaredField.getAnnotation(ManyToOne.class).name();
                    constraints = (canBeNullForManyToOne(declaredField) ? "" : " NOT NULL");
                    String referenceTableName = declaredField.getType().getAnnotation(Table.class).name();
                    String namedFk = declaredField.getType().getSimpleName().toLowerCase();
                    String fkStatement = String.format("CONSTRAINT fk_%s FOREIGN KEY(%s) REFERENCES %s(id)",
                            namedFk, columnName, referenceTableName);
                    String refActions = "\nON DELETE SET NULL\nON UPDATE CASCADE";
                    keys.add(fkStatement + refActions);
                }

                switch (fieldTypeName) {
                    case "String" -> columnNames.add(columnName + SQLDialect.STRING + constraints);
                    case "Long", "long" -> columnNames.add(columnName + SQLDialect.LONG + idAndPKTag + constraints);
                    case "int", "Integer" -> columnNames.add(columnName + SQLDialect.INTEGER + idAndPKTag + constraints);
                    case "LocalDate" -> columnNames.add(columnName + SQLDialect.LOCAL_DATE + constraints);
                    case "Boolean", "boolean" -> columnNames.add(columnName + SQLDialect.BOOLEAN + constraints);
                    case "Double", "double" -> columnNames.add(columnName + SQLDialect.DOUBLE + constraints);
                    case "foreign key" -> columnNames.add(columnName + SQLDialect.LONG + constraints);
                }
                columnNames.addAll(keys);
            }
        } else {
            throw new IdAnnotationNotFoundException(clss);
        }
        return columnNames;
    }

    public static boolean idAnnotationIsPresent(Class<?> clss) {
        boolean present = false;
        Field firstEntityField = clss.getDeclaredFields()[0];
        if (firstEntityField.isAnnotationPresent(Id.class)) {
            present = true;
        }
        return present;
    }

    public static String getColumnName(Field field) {
        String fieldName = "";
        if (field.isAnnotationPresent(ManyToOne.class)) {
            fieldName = getColumnNameFromManyToOne(field);
        }
        if (field.isAnnotationPresent(Column.class)) {
            fieldName = field.getAnnotation(Column.class).name();
        }
        return fieldName.equals("") ? field.getName() : fieldName;
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

    public static boolean isUnique(Field field) {
        return field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class).unique();
    }

    public static boolean canBeNull(Field field) {
        return field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class).nullable();
    }

    public static boolean canBeNullForManyToOne(Field field) {
        return field.getAnnotation(ManyToOne.class).nullable();
    }

    public static String getColumnNameFromManyToOne(Field field) {
        return field.getAnnotation(ManyToOne.class).name();
    }

}