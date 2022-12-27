package org.example.persistence.ormanager;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.model.Student;
import org.example.persistence.annotations.Entity;
import org.example.persistence.utilities.AnnotationUtils;
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
            if (cls.isAnnotationPresent(Entity.class)) {
                String tableName = getTableName(cls);

                Field[] declaredFields = cls.getDeclaredFields();
                List<String> columnNames = new ArrayList<>();

                for (Field field : declaredFields) {
                    Class<?> fieldType = field.getType();
                    if (!AnnotationUtils.getIdField(cls).equals("")) {
                        String name = field.getName();
                        AnnotationUtils.sqlColumnDeclaration(columnNames, fieldType, name, true, false);
                    } else {
                        String name = AnnotationUtils.getColumnName(field);
                        AnnotationUtils.sqlColumnDeclaration(columnNames, fieldType, name, AnnotationUtils.isUnique(field), AnnotationUtils.canBeNull(field));
                    }
                }
                String sqlCreateTable = String.format("%s %s%n(%n%s%n);", CREATE_TABLE, tableName,
                        String.join(",\n", columnNames));
                log.atDebug().log(sqlCreateTable);
                try (var prepStmt = dataSource.getConnection().prepareStatement(sqlCreateTable)) {
                    prepStmt.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
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
             PreparedStatement ps = connection.prepareStatement(getTableNameForInsert(o.getClass()), Statement.RETURN_GENERATED_KEYS)) {
            Field[] declaredFields = o.getClass().getDeclaredFields();
            declaredFields[2].setAccessible(true);
            ps.setString(1, declaredFields[2].get(o).toString());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()) {
                declaredFields[1].setAccessible(true);
                long generatedId = rs.getLong(1);
                declaredFields[1].set(o, generatedId);
            }
        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace();
        }
        log.info("created object: " + o.toString());
        SerializationUtil.serialize(o);
        return o;
    }

    private <T> boolean checkIfObjectExists(T o) {
        boolean exists = false;
        try {
            Field id = o.getClass().getDeclaredField("id");
            id.setAccessible(true);
            exists = id.get(o) != null;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return exists;
    }

    @Override
    public <T> Optional<T> findById(Serializable id, Class<T> cls) {
        T objectToFind;
        Constructor<T> declaredConstructor;
        Field[] declaredFields = cls.getDeclaredFields();
        try {
            declaredConstructor = cls.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            objectToFind = declaredConstructor.newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(getTableNameForSelect(cls))) {
            ps.setLong(1, (Long) id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                long personId = rs.getLong("id");
                String firstName = rs.getString("first_name");
                declaredFields[1].setAccessible(true);
                declaredFields[1].set(objectToFind, personId);
                declaredFields[2].setAccessible(true);
                declaredFields[2].set(objectToFind, firstName);
            }
        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return Optional.of(objectToFind);
    }

    @Override
    public <T> List<T> findAll(Class<T> cls) {
        List<Object> records = new ArrayList<>();
        try {
            Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement(SQL_FIND_ALL + getTableName(cls));
            ResultSet rs = st.executeQuery();
            ResultSetMetaData rsMetaData = rs.getMetaData();
            int columns = rsMetaData.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columns; i++) {
                    records.add(rsMetaData.getColumnName(i) + ": " + rs.getString(rsMetaData.getColumnName(i)));
                }
            }
            log.info(records.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return (List<T>) records;
    }

    @Override
    public <T> T update(T o) {
        return null;
    }

    @Override
    public <T> T refresh(T o) {
        return null;
    }

    @Override
    public boolean delete(Object o) {
        return false;
    }

}