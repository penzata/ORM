package org.example.persistence.utilities;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
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
     * @throws SQLException Need to initialize ORManager first to set the data source.
     */
    public static Connection getConnection() throws SQLException {
        return dataSource != null ? dataSource.getConnection() : null;
    }

    public static ORManager withDataSource(DataSource dataSource) {
        return new ORManagerImpl(dataSource);
    }

}