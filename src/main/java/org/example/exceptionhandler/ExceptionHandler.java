package org.example.exceptionhandler;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.SQLException;

@Slf4j
public class ExceptionHandler {

    private ExceptionHandler() {
    }

    public static void sql(SQLException ex) {
        log.atError().log("There's some problem with the database access" +
                " or the SQL statement:", ex);
    }

    public static void illegalAccess(ReflectiveOperationException ex) {
        log.atError().log("Either the underlying field/method is inaccessible or absent" +
                " or specified object argument is not an instance of the class" +
                " or interface declaring the underlying field/method:", ex);
    }

    public static void newInstance(ReflectiveOperationException ex) {
        log.atError().log("There's some problem initializing a new instance of the constructor's declaring class:", ex);
    }

    public static void inputOutput(IOException ex) {
        log.atError().log("An error occurred when reading from the input stream:", ex);
    }

}