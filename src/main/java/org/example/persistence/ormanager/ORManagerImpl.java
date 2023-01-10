package org.example.persistence.ormanager;

import lombok.extern.slf4j.Slf4j;
import org.example.exceptionhandler.EntityAnnotationNotFoundException;
import org.example.exceptionhandler.EntityNotFoundException;
import org.example.exceptionhandler.ExceptionHandler;
import org.example.persistence.annotations.Id;
import org.example.persistence.annotations.ManyToOne;
import org.example.persistence.annotations.OneToMany;

import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.example.persistence.sql.SQLDialect.*;
import static org.example.persistence.utilities.AnnotationUtils.*;


@Slf4j
public class ORManagerImpl implements ORManager {
    private DataSource dataSource;

    public ORManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static <T> Field getFieldWithOneToManyAnnotation(Class<T> clss) {
        Optional<Field> firstFoundField = Arrays.stream(clss.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(OneToMany.class))
                .findFirst();
        Field fieldWithIdAnnotation = firstFoundField.get();
        fieldWithIdAnnotation.setAccessible(true);
        return fieldWithIdAnnotation;
    }

    @Override
    public void register(Class... entityClasses) {
        for (Class<?> cls : entityClasses) {
            List<String> columnNames;
            String tableName = getTableName(cls);
            if (entityAnnotationIsPresent(cls)) {
                columnNames = declareColumnNamesFromEntityFields(cls);
                String sqlCreateTable = String.format("%s %s%n(%n%s%n);", SQL_CREATE_TABLE, tableName,
                        String.join(",\n", columnNames));
                String fk = createForeignKeyIfAvailable(cls);

                String registerTransaction = "BEGIN TRANSACTION;\n" + sqlCreateTable + (fk == null ? "" : fk) + "\nCOMMIT;";
                log.atInfo().log(registerTransaction);
                try (PreparedStatement prepStmt = dataSource.getConnection().prepareStatement(registerTransaction)) {
                    prepStmt.executeUpdate();
                } catch (SQLException e) {
                    ExceptionHandler.sql(e);
                }
            } else {
                throw new EntityAnnotationNotFoundException(cls);
            }
        }
    }

    public String createForeignKeyIfAvailable(Class<?> cls) {
        String fk = null;
        try (Statement stmt = dataSource.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery("SHOW TABLES;");
            while (rs.next()) {
                if (getTableName(cls).equalsIgnoreCase(rs.getString(1))) {
                    fk = createForeignKey(cls);
                }
            }
            rs.close();
        } catch (SQLException e) {
            ExceptionHandler.sql(e);
        }
        return fk;
    }

    @Override
    public <T> Optional<T> findById(Serializable id, Class<T> cls) {
        T entity = null;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlSelectStatement(cls))) {
            if (id.getClass().getSimpleName().equalsIgnoreCase("long")) {
                ps.setLong(1, (Long) id);
            } else {
                ps.setInt(1, (Integer) id);
            }
            ResultSet rs = ps.executeQuery();
            log.atInfo().log("{}", ps);
            while (rs.next()) {
                entity = extractEntityFromResultSet(rs, cls);
            }
            rs.close();
        } catch (SQLException e) {
            ExceptionHandler.sql(e);
        }
        return entity != null ? Optional.of(entity) : Optional.empty();
    }

    @Override
    public <T> T save(T o) {
        if (objectIdIsNotNull(o)) {
            return update(o);
        }
        persist(o);
        return o;
    }

    @Override
    public <T> T update(T o) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlUpdateStatement(o.getClass()))) {
            replacePlaceholdersInStatement(o, ps);
            int placeholderPositionForId = o.getClass().getDeclaredFields().length;
            Field fieldWithIdAnnotation = getFieldWithIdAnnotation(o.getClass());
            if (fieldWithIdAnnotation.get(o) == null) {
                throw new EntityNotFoundException(o);
            }
            ps.setString(placeholderPositionForId, fieldWithIdAnnotation.get(o).toString());
            ps.executeUpdate();
            removeObjectToOneToManyField(o);
            addObjectToOneToManyField(o);
            log.atInfo().log("{}", ps);
        } catch (SQLException ex) {
            ExceptionHandler.sql(ex);
        } catch (IllegalAccessException e) {
            ExceptionHandler.illegalAccess(e);
        }
        return o;
    }

    @Override
    public void persist(Object o) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlInsertStatement(o.getClass()), Statement.RETURN_GENERATED_KEYS)) {
            replacePlaceholdersInStatement(o, ps);
            ps.executeUpdate();
            log.atInfo().log("{}", ps);
            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()) {
                setAutoGeneratedId(o, rs);
            }
            addObjectToOneToManyField(o);
            rs.close();
        } catch (SQLException e) {
            ExceptionHandler.sql(e);
        }
    }

    public <T> void addObjectToOneToManyField(T o) {
        Field[] declaredFields = o.getClass().getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            declaredFields[i].setAccessible(true);
            if (declaredFields[i].isAnnotationPresent(ManyToOne.class)) {
                Class<?> type = declaredFields[i].getType();
                Field fieldWIthOneToManyAnnotation = getFieldWithOneToManyAnnotation(declaredFields[i].getType());
                List<T> list;
                try {
                    if (declaredFields[i].get(o) != null) {
                        list = (List<T>) fieldWIthOneToManyAnnotation.get(declaredFields[i].get(o));
                        fieldWIthOneToManyAnnotation.set(declaredFields[i].get(o), list);
                        list.add(o);
                    }
                } catch (IllegalAccessException e) {
                    ExceptionHandler.illegalAccess(e);
                }
            }
        }
    }

    public <T> void removeObjectToOneToManyField(T o) {
        Field[] declaredFields = o.getClass().getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            declaredFields[i].setAccessible(true);
            if (declaredFields[i].isAnnotationPresent(ManyToOne.class)) {
                Class<?> type = declaredFields[i].getType();
                Field fieldWIthOneToManyAnnotation = getFieldWithOneToManyAnnotation(declaredFields[i].getType());
                List<T> list;
                try {
                    if (declaredFields[i].get(o) != null) {
                        list = (List<T>) fieldWIthOneToManyAnnotation.get(declaredFields[i].get(o));
                        fieldWIthOneToManyAnnotation.set(declaredFields[i].get(o), list);
                        list.remove(o);
                    }
                } catch (IllegalAccessException e) {
                    ExceptionHandler.illegalAccess(e);
                }
            }
        }
    }

    @Override
    public <T> List<T> findAll(Class<T> cls) {
        List<T> records = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(sqlSelectAllStatement(cls))) {
            log.atInfo().log("{}", st);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                records.add(extractEntityFromResultSet(rs, cls));
            }
            rs.close();
        } catch (SQLException e) {
            ExceptionHandler.sql(e);
        }
        return records;
    }

    @Override
    public long recordsCount(Class<?> clss) {
        long count = 0;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlCountStatement(clss))) {
            ResultSet rs = ps.executeQuery();
            log.atInfo().log("{}", ps);
            if (rs.next()) {
                count = rs.getLong(1);
            }
        } catch (SQLException e) {
            ExceptionHandler.sql(e);
        }
        return count;
    }

    @Override
    public <T> T refresh(T o) {
        Field[] declaredFields = o.getClass().getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            declaredFields[i].setAccessible(true);
        }
        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement(sqlSelectStatement(o.getClass()), Statement.RETURN_GENERATED_KEYS)) {
            Object valueOfIDField = getFieldWithIdAnnotation(o.getClass()).get(o);
            if (valueOfIDField != null) {
                st.setString(1, valueOfIDField.toString());
                log.atInfo().log("{}", st);
                ResultSet rs = st.executeQuery();
                ResultSetMetaData rsMt = rs.getMetaData();
                while (rs.next()) {
                    for (int i = 1; i < rsMt.getColumnCount(); i++) {
                        if (rsMt.getColumnName(i).equalsIgnoreCase("id")) {
                            continue;
                        }
                        switch (rsMt.getColumnTypeName(i)) {
                            case "CHARACTER VARYING" ->
                                    declaredFields[i - 1].set(o, rs.getString(rsMt.getColumnName(i)));
                            case "INTEGER" -> declaredFields[i - 1].set(o, rs.getInt(rsMt.getColumnName(i)));
                            case "BIGINT" -> declaredFields[i - 1].set(o, rs.getLong(rsMt.getColumnName(i)));
                            case "DOUBLE PRECISION" ->
                                    declaredFields[i - 1].set(o, rs.getDouble(rsMt.getColumnName(i)));
                            case "BOOLEAN" -> declaredFields[i - 1].set(o, rs.getBoolean(rsMt.getColumnName(i)));
                            case "DATE" -> {
                                Date sqlDate = rs.getDate(rsMt.getColumnName(i));
                                if (sqlDate != null) {
                                    LocalDate sqlLocalDate = sqlDate.toLocalDate();
                                    declaredFields[i - 1].set(o, sqlLocalDate);
                                }
                            }
                        }
                    }
                }
            } else {
                throw new EntityNotFoundException(o);
            }
        } catch (SQLException e) {
            ExceptionHandler.sql(e);
        } catch (IllegalAccessException e) {
            ExceptionHandler.illegalAccess(e);
        }
        return o;
    }

    private static <T> Field getFieldWithIdAnnotation(Class<T> clss) {
        Optional<Field> firstFoundField = Arrays.stream(clss.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .findFirst();
        Field fieldWithIdAnnotation = firstFoundField.get();
        fieldWithIdAnnotation.setAccessible(true);
        return fieldWithIdAnnotation;
    }

    @Override
    public void delete(Object... objects) {
        for (Object object : objects) {
            delete(object);
        }
    }

    @Override
    public boolean delete(Object o) {
        Field fieldWithIdAnnotation = getFieldWithIdAnnotation(o.getClass());
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlDeleteStatement(o.getClass()))) {
            if (fieldWithIdAnnotation.get(o) == null) {
                return false;
            }
            ps.setString(1, fieldWithIdAnnotation.get(o).toString());
            ps.executeUpdate();
            log.atInfo().log("{}", ps);
            fieldWithIdAnnotation.set(o, null);
            return true;
        } catch (SQLException e) {
            ExceptionHandler.sql(e);
        } catch (IllegalAccessException e) {
            ExceptionHandler.illegalAccess(e);
        }
        return false;
    }

    private <T> boolean objectIdIsNotNull(T o) {
        boolean exists = false;
        try {
            Field fieldWithIdAnnotation = getFieldWithIdAnnotation(o.getClass());
            exists = fieldWithIdAnnotation.get(o) != null;
        } catch (IllegalAccessException e) {
            ExceptionHandler.illegalAccess(e);
        }
        return exists;
    }

    /**
     * Replaces all placeholders ("?") in INSERT and UPDATE(except for the last one: WHERE id = ?) sql statements
     * with the names of the columns in the table extracted form the saved/updated object.
     *
     * @param o  The generic entity object which will be saved or updated in the DB.
     * @param ps Prepared statement for Insert and Update.
     * @throws SQLException
     */
    private <T> void replacePlaceholdersInStatement(T o, PreparedStatement ps) throws SQLException {
        try {
            Field[] declaredFields = o.getClass().getDeclaredFields();
            int parameterIndex = 0;
            for (int i = 0; i < declaredFields.length; i++) {
                if (declaredFields[i].isAnnotationPresent(Id.class) || declaredFields[i].isAnnotationPresent(OneToMany.class)) {
                    continue;
                }
                declaredFields[i].setAccessible(true);
                String fieldTypeName = declaredFields[i].getType().getSimpleName();
                parameterIndex += 1;
                if (declaredFields[i].get(o) == null) {
                    ps.setObject(parameterIndex, null);
                    continue;
                }
                switch (fieldTypeName) {
                    case "String" -> ps.setString(parameterIndex, declaredFields[i].get(o).toString());
                    case "Long", "long" -> ps.setLong(parameterIndex, (Long) declaredFields[i].get(o));
                    case "Integer", "int" -> ps.setInt(parameterIndex, (Integer) declaredFields[i].get(o));
                    case "Boolean", "boolean" -> ps.setBoolean(parameterIndex, (Boolean) declaredFields[i].get(o));
                    case "Double", "double" -> ps.setDouble(parameterIndex, (Double) declaredFields[i].get(o));
                    case "LocalDate" -> ps.setDate(parameterIndex, Date.valueOf(declaredFields[i].get(o).toString()));
                    case "List", "ArrayList" -> {
                    }
                    default -> {
                        Field fieldWithIdAnnotation = getFieldWithIdAnnotation(declaredFields[i].getType());
                        Object objectFieldIdValue = fieldWithIdAnnotation.get(declaredFields[i].get(o));
                        ps.setObject(parameterIndex, objectFieldIdValue);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            ExceptionHandler.illegalAccess(e);
        }
    }

    /**
     * Sets the ID field value of generic entity object to the autogenerated one from the DB side.
     *
     * @param o            The generic entity object for which the ID value will be set.
     * @param generatedKey ResultSet from INSERT sql statement containing the autogenerated keys from DB side.
     * @throws SQLException
     */
    private <T> void setAutoGeneratedId(T o, ResultSet generatedKey) throws SQLException {
        Field fieldWithIdAnnotation = getFieldWithIdAnnotation(o.getClass());
        String fieldTypeSimpleName = fieldWithIdAnnotation.getType().getSimpleName();
        try {
            if (fieldTypeSimpleName.equals("Long")) {
                fieldWithIdAnnotation.set(o, generatedKey.getLong(1));
            } else {
                fieldWithIdAnnotation.set(o, generatedKey.getInt(1));
            }
        } catch (IllegalAccessException e) {
            ExceptionHandler.illegalAccess(e);
        }
    }

    /**
     * @param rs   ResultSet from SELECT sql statement.
     * @param clss Class.
     * @return Generic entity object with field values set from the DB record value.
     * @throws SQLException
     */
    private <T> T extractEntityFromResultSet(ResultSet rs, Class<T> clss) throws SQLException {
        T entityToFind = createNewInstance(clss);
        try {
            Field[] declaredFields = clss.getDeclaredFields();
            for (int i = 0; i < declaredFields.length; i++) {
                declaredFields[i].setAccessible(true);
                String fieldTypeName = declaredFields[i].getType().getSimpleName();
                int columnIndex = i + 1;
                switch (fieldTypeName) {
                    case "String" -> declaredFields[i].set(entityToFind, rs.getString(columnIndex));
                    case "Long", "long" -> declaredFields[i].set(entityToFind, rs.getLong(columnIndex));
                    case "Integer", "int" -> declaredFields[i].set(entityToFind, rs.getInt(columnIndex));
                    case "Boolean", "boolean" -> declaredFields[i].set(entityToFind, rs.getBoolean(columnIndex));
                    case "Double", "double" -> declaredFields[i].set(entityToFind, rs.getDouble(columnIndex));
                    case "LocalDate" -> {
                        if (rs.getDate(columnIndex) != null) {
                            declaredFields[i].set(entityToFind, rs.getDate(columnIndex).toLocalDate());
                        } else {
                            declaredFields[i].set(entityToFind, rs.getDate(columnIndex));
                        }
                    }
                    case "List", "ArrayList" -> {
                    }
                    default -> {
                        Long columnValue = rs.getLong(columnIndex);
                        if (columnValue != 0) {
                            Object byId = findById(columnValue, declaredFields[i].getType()).get();
                            declaredFields[i].set(entityToFind, byId);
                        } else {
                            declaredFields[i].set(entityToFind, null);
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            ExceptionHandler.illegalAccess(e);
        }
        return entityToFind;
    }

    /**
     * Create and initialize a new instance from the no-args constructor of the provided class.
     *
     * @param cls Class.
     * @return New empty generic object of the given class.
     */
    private <T> T createNewInstance(Class<T> cls) {
        T newObject = null;
        try {
            Constructor<T> declaredConstructor = cls.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            newObject = declaredConstructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            ExceptionHandler.newInstance(e);
        }
        return newObject;
    }
}