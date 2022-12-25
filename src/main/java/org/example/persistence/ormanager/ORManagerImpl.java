package org.example.persistence.ormanager;

import com.zaxxer.hikari.HikariDataSource;
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
import java.util.Map;
import java.util.Optional;
import static org.example.persistence.utilities.Utils.getConnection;
import static org.example.persistence.utilities.Utils.getTableName;

@Slf4j
public class ORManagerImpl implements ORManager {
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS";
    private static final String SQL_INSERT_STUDENT = """
            INSERT INTO STUDENTS (first_name) values(?)
            """;

    private static final String ID = " BIGINT PRIMARY KEY AUTO_INCREMENT";
    private static final String NAME = " VARCHAR(255) UNIQUE NOT NULL";
    private static final String DATE = " DATE NOT NULL";
    private static final String INT = " INT NOT NULL";

    private DataSource dataSource;

    public ORManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    static DataSource createDataSource(String url, String user, String password, Map<String, String> props) throws SQLException {
        var dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        Connection connection = DriverManager.getConnection(url, user, password);
        connection.setAutoCommit(true);
        log.info("Connection valid: " + connection.isValid(1000));
        return dataSource;
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
                        getColumnName(sql, fieldType, name);
                    } else if (field.isAnnotationPresent(Column.class)) {
                        String name = getFieldName(field);
                        getColumnName(sql, fieldType, name);
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

    private void getColumnName(ArrayList<String> sql, Class<?> type, String name) {
        if (type == Long.class) {
            sql.add(name + ID);
        }
        if (type == String.class) {
            sql.add(name + NAME);
        } else if (type == LocalDate.class) {
            sql.add(name + DATE);
        } else if (type == int.class) {
            sql.add(name + INT);
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
