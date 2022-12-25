package org.example.persistence.ormanager;

import lombok.extern.slf4j.Slf4j;
import org.example.persistence.annotations.Column;
import org.example.persistence.annotations.Entity;
import org.example.persistence.annotations.Id;

import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Field;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.example.persistence.utilities.Utils.getConnection;
import static org.example.persistence.utilities.Utils.getTableName;

@Slf4j
public class ORManagerImpl implements ORManager {
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS";
    private static final String SQL_INSERT_STUDENT = """
            INSERT INTO STUDENTS (first_name) values(?)
            """;
    private static final String SQL_FIND_ALL = """
            SELECT * FROM
            """;

    private static final String ID = " BIGINT PRIMARY KEY AUTO_INCREMENT";
    private static final String NAME = " VARCHAR(255) ";
    private static final String DATETIME = " DATETIME ";
    private static final String INT = " INT ";
    private static final String BOOLEAN = " BOOLEAN ";
    private static final String NOT_NULL = "NOT NULL ";

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
                ArrayList<String> sql = new ArrayList<>();

                for (Field field : declaredFields) {
                    Class<?> fieldType = field.getType();
                    if (field.isAnnotationPresent(Id.class)) {
                        String name = field.getName();
                        setColumnName(sql, fieldType, name);
                    } else if (field.isAnnotationPresent(Column.class)) {
                        Column columnAnn = field.getAnnotation(Column.class);
                        String name = getFieldName(field);
                        if (columnAnn.nullable()) {//can be null
                            if (columnAnn.unique()) {
                                setColumnName(sql, fieldType, name + " NOT NULL");
                            } else {
                                setColumnName(sql, fieldType, name);
                            }
                        } else {//not null
                            setColumnNameNotNull(sql, fieldType, name);
                        }
                    }
                }
                String sqlCreateTable = String.format("%s %s(%s);", CREATE_TABLE, tableName,
                        String.join(", ", sql));

                try (var prepStmt = getConnection().prepareStatement(sqlCreateTable)) {
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
        List<String> records = new ArrayList<>();
        try {
            Connection connection = dataSource.getConnection();
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(SQL_FIND_ALL + getTableName(cls));
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

    private void setColumnName(ArrayList<String> sql, Class<?> type, String name) {
        if (type == Long.class) {
            sql.add(name + ID);
        }
        if (type == String.class) {
            sql.add(name + NAME);
        } else if (type == LocalDate.class) {
            sql.add(name + DATETIME);
        } else if (type == int.class) {
            sql.add(name + INT);
        }
    }

    private void setColumnNameNotNull(ArrayList<String> sql, Class<?> type, String name) {
        if (type == Long.class) {
            sql.add(name + ID);
        }
        if (type == String.class) {
            sql.add(name + NAME + NOT_NULL);
        } else if (type == LocalDate.class) {
            sql.add(name + DATETIME + NOT_NULL);
        } else if (type == int.class) {
            sql.add(name + INT + NOT_NULL);
        }
    }


    private String getFieldName(Field field) {
        String name = field.getAnnotation(Column.class).name();
        if (name.equals("")) {
            name = field.getName();
        }
        return name;
    }
}
