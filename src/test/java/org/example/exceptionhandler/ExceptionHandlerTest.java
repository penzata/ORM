package org.example.exceptionhandler;

import org.example.domain.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ExceptionHandlerTest {
    Student student;
    Field[] declaredFields;
    Constructor<? extends Student> declaredConstructor;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        student = new Student("Pinky", "One", 3, LocalDate.now());
        declaredFields = student.getClass().getDeclaredFields();
        declaredConstructor = student.getClass().getDeclaredConstructor();
    }

    @Test
    void sql() {
        SQLException sqlException = assertThrows(SQLException.class, () -> DriverManager.getConnection("'not real url'"));

        ExceptionHandler.sql(sqlException);
    }

    @Test
    void illegalAccess() {
        ReflectiveOperationException exception = assertThrows(ReflectiveOperationException.class, () -> declaredFields[0].set(student, 4));

        ExceptionHandler.illegalAccess(exception);
    }

    @Test
    void newInstance() {
        ReflectiveOperationException exception = assertThrows(ReflectiveOperationException.class, () -> declaredConstructor.newInstance());

        ExceptionHandler.newInstance(exception);
    }

    @Test
    void inputOutput() throws IOException {
        InputStream resourceAsStream = getClass().getResourceAsStream("/db.properties");
        resourceAsStream.close();

        IOException ioException = assertThrows(IOException.class, () -> new Properties().load(resourceAsStream));

        ExceptionHandler.inputOutput(ioException);
    }
}