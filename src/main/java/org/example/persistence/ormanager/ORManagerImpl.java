package org.example.persistence.ormanager;

import com.zaxxer.hikari.HikariDataSource;
import org.example.domain.model.Student;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ORManagerImpl implements ORManager {
    private static final Logger logger = Logger.getLogger(ORManagerImpl.class.getName());
    private DataSource dataSource;
    private Connection connection;
    private Statement statement;
    private PreparedStatement ps;

    private static final String SQL_CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS STUDENTS (first_name VARCHAR(255))
            """;
    private static final String SQL_INSERT_STUDENT = """
            INSERT INTO STUDENTS (first_name) values(?)
            """;

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
        logger.info("Connection valid: " + connection.isValid(1000));
        return dataSource;
    }

    @Override
    public void register(Class... entityClasses) {
        
    }

    @Override
    public <Student> Student save(Student student) {
        try {
            if(student instanceof org.example.domain.model.Student st) {
                connection = dataSource.getConnection();
                statement = connection.createStatement();
                statement.execute(SQL_CREATE_TABLE);
                logger.log(Level.INFO, "table created");
                ps = connection.prepareStatement(SQL_INSERT_STUDENT);
                ps.setString(1, st.getFirstName());
                int rows = ps.executeUpdate();
                logger.log(Level.INFO, rows + " rows affected");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return student;
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
}
