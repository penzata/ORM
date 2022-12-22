package org.example.persistence.ormanager;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

public class ORManagerImpl implements ORManager{

    private DataSource dataSource;

    public ORManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    static DataSource createDataSource(String url, String user, String password, Map<String, String> props) throws SQLException {
        var dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        Connection connection = DriverManager.getConnection(url,user,password);
        connection.setAutoCommit(true);

        System.out.println(connection.isValid(1000));
        return dataSource;
    }
}
