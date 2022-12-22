package org.example.persistence.ormanager;

import com.zaxxer.hikari.HikariDataSource;
import org.example.domain.model.Student;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.sql.*;
import java.util.Map;

public class ORManagerImpl implements org.example.persistence.ormanager.ORManager {

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
    @Override
    public Student save(Student student){
        Connection connection;
        try {
           connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS STUDENTS (first_name VARCHAR(255)) ;");
            System.out.println("table created");
           PreparedStatement ps =  connection.prepareStatement("INSERT INTO STUDENTS (first_name) values(?)");
           ps.setString(1, student.getFirstName());
           int rows = ps.executeUpdate();
            System.out.println(rows + " rows affected.");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return student;
    }
}
