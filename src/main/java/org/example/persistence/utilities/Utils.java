package org.example.persistence.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Utils {
    public Utils() {
    }

    public static void main(String[] args) throws SQLException {
        Path path = Path.of("db/properties/h2.properties");
        Properties properties = readProperties(path);
        String databaseUrl = properties.getProperty("jdbc-url");
        System.out.println(databaseUrl);
        Connection connection = DriverManager.getConnection(databaseUrl);
        connection.setAutoCommit(true);

        System.out.println(connection.isValid(1000));
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
}