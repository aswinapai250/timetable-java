package com.asiet.aids.timetable.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Single place responsible for opening a JDBC connection to MySQL.
 * Reads credentials from src/main/resources/db.properties so nobody
 * hardcodes a password inside a class file.
 */
public class DBConnection {

    private static final String CONFIG_FILE = "db.properties";
    private static Properties props;

    private static Properties loadProperties() {
        if (props == null) {
            props = new Properties();
            try (InputStream in = DBConnection.class.getClassLoader()
                    .getResourceAsStream(CONFIG_FILE)) {
                if (in == null) {
                    throw new RuntimeException(
                        "Could not find " + CONFIG_FILE + " on classpath. " +
                        "Make sure it's in src/main/resources/");
                }
                props.load(in);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load " + CONFIG_FILE, e);
            }
        }
        return props;
    }

    /**
     * Opens and returns a new JDBC Connection. Caller is responsible for
     * closing it (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        Properties p = loadProperties();
        String url = p.getProperty("db.url");
        String user = p.getProperty("db.user");
        String password = p.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }
}
