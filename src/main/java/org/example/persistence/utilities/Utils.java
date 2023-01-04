package org.example.persistence.utilities;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.example.exceptionhandler.ExceptionHandler;
import org.example.persistence.ormanager.ORManager;
import org.example.persistence.ormanager.ORManagerImpl;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
public class Utils {
    private static HikariDataSource dataSource;

    private Utils() {
    }

    public static ORManager withPropertiesFrom(String fileName) {
        Path path = Path.of(fileName);
        Properties properties = readProperties(path);

        String jdbcUrl = properties.getProperty("jdbc-url");
        String jdbcUser = properties.getProperty("jdbc-username", "");
        String jdbcPass = properties.getProperty("jdbc-pass", "");

        return new ORManagerImpl(createDataSource(jdbcUrl, jdbcUser, jdbcPass));
    }

    private static Properties readProperties(Path file) {
        Properties result = new Properties();
        try (InputStream inputStream = Utils.class.getClassLoader().getResourceAsStream(file.toString())) {
            result.load(inputStream);
            return result;
        } catch (IOException e) {
            ExceptionHandler.inputOutput(e);
        }
        return result;
    }

    private static DataSource createDataSource(String url, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        dataSource = new HikariDataSource(config);
        return dataSource;
    }

    /**
     * Need to initialize ORManager first to set this data source or the connection will be null.
     * @return connection from the datasource, provided by the created ORM Manager.
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        return dataSource != null ? dataSource.getConnection() : null;
    }

    public static ORManager withDataSource(DataSource dataSource) {
        return new ORManagerImpl(dataSource);
    }
}