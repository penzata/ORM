package org.example.persistence.ormanager;

import com.zaxxer.hikari.HikariDataSource;
import org.example.domain.model.Student;

import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ORManagerImpl implements ORManager {
    private static final Logger logger = Logger.getLogger(ORManagerImpl.class.getName());
    private DataSource dataSource;
    private static final String SQL_CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS STUDENTS (id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, first_name VARCHAR(255))
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
    public <T> T save(T o) {
        try {
            Field[] declaredFields = o.getClass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
            }
                Connection connection  = dataSource.getConnection();
               Statement statement = connection.createStatement();
                statement.execute(SQL_CREATE_TABLE);
               PreparedStatement ps = connection.prepareStatement(SQL_INSERT_STUDENT, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, declaredFields[1].get(o).toString());
                int rows = ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                var next = rs.next();
                while(rs.next()){
                    long id = rs.getLong(1);
                    logger.info(declaredFields[1].toString());
                    declaredFields[0].set(o,id);
                    System.out.println(id);
                }
                logger.log(Level.INFO, rows + " rows affected");
            System.out.println(o);


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
}
