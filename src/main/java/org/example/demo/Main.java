package org.example.demo;

import org.example.persistence.ormanager.ORManager;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        String path = "db/properties/h2.properties";

        ORManager orManager = ORManager.withPropertiesFrom(path);

    }
}