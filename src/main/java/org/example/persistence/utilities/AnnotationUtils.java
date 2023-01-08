package org.example.persistence.utilities;

import org.example.persistence.annotations.*;
import org.example.persistence.sql.SQLDialect;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
        for (Field declaredField : clss.getDeclaredFields()) {
            String fieldTypeName = declaredField.getType().getSimpleName();
            String columnName = getColumnName(declaredField);
            String idAndPKTag = autoincrementPrimaryKeyTag(declaredField);
            String constraints = (isUnique(declaredField) ? " UNIQUE " : "") +
                    (canBeNull(declaredField) ? "" : " NOT NULL");

            if (declaredField.isAnnotationPresent(ManyToOne.class)) {
                fieldTypeName = "manyToOne";
                columnName = declaredField.getAnnotation(ManyToOne.class).name();
                constraints = (canBeNullForManyToOne(declaredField) ? "" : " NOT NULL");
            }

            switch (fieldTypeName) {
                case "String" -> columnNames.add(columnName + SQLDialect.STRING + constraints);
                case "Long", "long" -> columnNames.add(columnName + SQLDialect.LONG + idAndPKTag + constraints);
                case "int", "Integer" -> columnNames.add(columnName + SQLDialect.INTEGER + idAndPKTag + constraints);
                case "LocalDate" -> columnNames.add(columnName + SQLDialect.LOCAL_DATE + constraints);
                case "Boolean", "boolean" -> columnNames.add(columnName + SQLDialect.BOOLEAN + constraints);
                case "Double", "double" -> columnNames.add(columnName + SQLDialect.DOUBLE + constraints);
                case "manyToOne" -> columnNames.add(columnName + SQLDialect.LONG + constraints);
            }
        }
        return columnNames;
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

    public static String getColumnNameFromManyToOne(Field field) {
        return field.getAnnotation(ManyToOne.class).name();
    }

    public static String getColumnNameFromManyToOne(Class<?> cls) {
        for (Field declaredField : cls.getDeclaredFields()) {
            if (declaredField.isAnnotationPresent(ManyToOne.class)) {
                return declaredField.getAnnotation(ManyToOne.class).name();
            }
        }
        return null;
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

    public static <T> Class<?> getListType (Field field){
        ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
        Class<?> stringListClass = (Class<?>) stringListType.getActualTypeArguments()[0];

        return stringListClass;
    }



    public static String getReferencedTableName(Class<?> cls){
        String tableName = null;
        for (Field declaredField : cls.getDeclaredFields()) {
            tableName = getColumnName(declaredField);
        }
        return tableName;
    }

    public static String createForeignKey(Class<?> cls) {
        for (Field declaredField : cls.getDeclaredFields()) {
            if (declaredField.isAnnotationPresent(OneToMany.class)) {
                Class<?> listType = getListType(declaredField);
                String fkColumnName = getColumnNameFromManyToOne(listType);
                String fk = "ALTER TABLE " + getColumnName(declaredField) + " ADD FOREIGN KEY (" + fkColumnName + ") REFERENCES " + getTableName(cls) + "(id) ON DELETE SET NULL ON UPDATE CASCADE;";
                System.out.println("key: " + fk);
                return fk;
            } else if (declaredField.isAnnotationPresent(ManyToOne.class)) {
                Class<?> referencedTable = declaredField.getType();
                String fk = "ALTER TABLE " + getTableName(cls) + " ADD FOREIGN KEY (" + getColumnName(declaredField) + ") REFERENCES " + getTableName(referencedTable) + "(id) ON DELETE SET NULL ON UPDATE CASCADE;";
                System.out.println("key: " + fk);
                return fk;
            }
        }
        return null;
    }

}