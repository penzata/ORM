package org.example.persistence.utilities;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.example.persistence.annotations.Column;
import org.example.persistence.annotations.Table;
import org.example.persistence.ormanager.ORManager;
import org.example.persistence.ormanager.ORManagerImpl;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Properties;

import static org.example.persistence.sql.SQLDialect.*;

@Slf4j
public class Utils {
    private static HikariDataSource dataSource;

    private Utils() {
    }

    public static ORManager withPropertiesFrom(String filename) {
        Path path = Path.of(filename);
        Properties properties = readProperties(path);

        String jdbcUrl = properties.getProperty("jdbc-url");
        String jdbcUser = properties.getProperty("jdbc-username", "");
        String jdbcPass = properties.getProperty("jdbc-pass", "");

        return new ORManagerImpl(createDataSource(jdbcUrl, jdbcUser, jdbcPass));
    }

    public static Properties readProperties(Path file) {
        Properties result = new Properties();
        try (InputStream inputStream = Utils.class.getClassLoader().getResourceAsStream(file.toString())) {
            result.load(inputStream);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static DataSource createDataSource(String url, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        HikariDataSource dataSource = new HikariDataSource(config);
        setDataSourceFromORMCreation(dataSource);
        return dataSource;
    }

    private static void setDataSourceFromORMCreation(HikariDataSource dataSource) {
        Utils.dataSource = dataSource;
    }

    /**
     * @return connection from the datasource, provided by the created ORM Manager.
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        try {
            return dataSource.getConnection();
        } catch (NullPointerException e) {
            log.error("Need to initialize ORManager first to set the data source.");
        }
        return null;
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

    public static void setColumnName(ArrayList<String> sql, Class<?> type, String name, boolean isUnique, boolean canBeNull) {
        String constraints =
                (isUnique ? " UNIQUE " : "") +
                        (canBeNull ? "" : "NOT NULL");

        if (type == Long.class) {
            sql.add(name + ID);
        }
        if (type == String.class) {
            sql.add(name + NAME + constraints);
        } else if (type == LocalDate.class) {
            sql.add(name + DATETIME + constraints);
        } else if (type == int.class) {
            sql.add(name + INT + constraints);
        } else if (type == boolean.class) {
            sql.add(name + BOOLEAN + constraints);
        }
    }

    public static ORManager withDataSource(DataSource dataSource) {
        return new ORManagerImpl(dataSource);
    }

}