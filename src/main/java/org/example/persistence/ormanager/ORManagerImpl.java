package org.example.persistence.ormanager;

import lombok.extern.slf4j.Slf4j;
import org.example.persistence.annotations.Entity;
import org.example.persistence.utilities.SerializationUtil;

import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.example.persistence.sql.SQLDialect.*;
import static org.example.persistence.utilities.AnnotationUtils.declareColumnNamesFromEntityFields;
import static org.example.persistence.utilities.AnnotationUtils.getTableName;


@Slf4j
public class ORManagerImpl implements ORManager {
    private DataSource dataSource;

    public ORManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void register(Class... entityClasses) {
        for (Class<?> cls : entityClasses) {
            List<String> columnNames;
            String tableName = getTableName(cls);
            if (cls.isAnnotationPresent(Entity.class)) {
                columnNames = declareColumnNamesFromEntityFields(cls);
                String sqlCreateTable = String.format("%s %s%n(%n%s%n);", CREATE_TABLE, tableName,
                        String.join(",\n", columnNames));
                log.atDebug().log(sqlCreateTable);
                try (PreparedStatement prepStmt = dataSource.getConnection().prepareStatement(sqlCreateTable)) {
                    prepStmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public <T> T save(T o) {
        if (checkIfObjectExists(o)) {
            return o;
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(getTableAndColumnNamesForInsert(o.getClass()), Statement.RETURN_GENERATED_KEYS)) {
            Field[] declaredFields = o.getClass().getDeclaredFields();
            declaredFields[1].setAccessible(true);
            ps.setString(1, declaredFields[1].get(o).toString());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()) {
                declaredFields[0].setAccessible(true);
                long generatedId = rs.getLong(1);
                declaredFields[0].set(o, generatedId);
            }
        } catch (SQLException | IllegalAccessException e) {
            log.error("SQL & IllegalAccessException exceptions", e);
        }
        SerializationUtil.serialize(o);
        return o;
    }

    private <T> boolean checkIfObjectExists(T o) {
        boolean exists = false;
        try {
            Field[] declaredFields = o.getClass().getDeclaredFields();
            declaredFields[0].setAccessible(true);
            exists = declaredFields[0].get(o) != null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return exists;
    }

    @Override
    public <T> Optional<T> findById(Serializable id, Class<T> cls) {
        T objectToFind = null;
        Field[] declaredFields = cls.getDeclaredFields();
        try {
            Constructor<T> declaredConstructor = cls.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            objectToFind = declaredConstructor.newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(getTableNameForSelect(cls))) {
            ps.setLong(1, (Long) id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                long personId = rs.getLong(1);
                String firstName = rs.getString(2);
                declaredFields[0].setAccessible(true);
                declaredFields[0].set(objectToFind, personId);
                declaredFields[1].setAccessible(true);
                declaredFields[1].set(objectToFind, firstName);
            }
        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(objectToFind);
    }

    @Override
    public <T> T update(T o) {
        try (Connection conn = dataSource.getConnection()) {
            Field[] fields = o.getClass().getDeclaredFields();
            fields[0].setAccessible(true);
            fields[1].setAccessible(true);
            PreparedStatement ps = conn.prepareStatement(UPDATE_STUDENT);
            ps.setString(1, fields[1].get(o).toString());
            ps.setString(2, fields[0].get(o).toString());
            ps.executeUpdate();

        } catch (SQLException | IllegalAccessException ex) {
            log.info("Exception has occurred: ", ex);
        }
        return o;
    }

    @Override
    public <T> T refresh(T o) {
        return null;
    }

    @Override
    public int recordsCount(Class<?> clss) {
        return findAll(clss).size();
    }

    @Override
    public <T> List<T> findAll(Class<T> cls) {
        List<T> records = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(SQL_FIND_ALL + getTableName(cls))) {
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Constructor<T> objDeclaredConstructor = cls.getDeclaredConstructor();
                objDeclaredConstructor.setAccessible(true);
                T myObj = objDeclaredConstructor.newInstance();
                Field[] fields = myObj.getClass().getDeclaredFields();
                fields[0].setAccessible(true);
                fields[0].set(myObj, rs.getLong(1));
                fields[1].setAccessible(true);
                fields[1].set(myObj, rs.getString(2));
                records.add(myObj);
            }
            log.info("all records: {}", records);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return records;
    }

    @Override
    public void delete(Object... objects) {
        for (Object object : objects) {
            delete(object);
        }
    }

    @Override
    public boolean delete(Object o) {
        return false;
    }
}