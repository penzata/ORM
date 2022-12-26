package org.example.persistence.ormanager;

import lombok.extern.slf4j.Slf4j;
import org.example.persistence.annotations.Entity;
import org.example.persistence.utilities.AnnotationUtils;

import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.example.persistence.sql.SQLDialect.CREATE_TABLE;
import static org.example.persistence.sql.SQLDialect.SQL_INSERT_STUDENT;

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
                String tableName = AnnotationUtils.getTableName(cls);

                Field[] declaredFields = cls.getDeclaredFields();
                List<String> columnNames = new ArrayList<>();

                for (Field field : declaredFields) {
                    Class<?> fieldType = field.getType();
                    if (!AnnotationUtils.getIdField(field).equals("")) {
                        String name = field.getName();
                        AnnotationUtils.setSqlColumnStatement(columnNames, fieldType, name, true, false);
                    } else {
                        String name = AnnotationUtils.getColumnName(field);
                        AnnotationUtils.setSqlColumnStatement(columnNames, fieldType, name, AnnotationUtils.isUnique(field), AnnotationUtils.canBeNull(field));
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
        try {
            Field[] declaredFields = o.getClass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
            }
            Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT_STUDENT, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, declaredFields[1].get(o).toString());
            int rows = ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()) {
                log.info(declaredFields[1].getName());
                declaredFields[0].set(o, rs.getLong(1));
            }
            log.info(rows + " rows affected");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }

    @Override
    public <T> Optional<T> findById(Serializable id, Class<T> cls) {
        try {
            Connection connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public <T> List<T> findAll(Class<T> cls) {
        return null;
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
