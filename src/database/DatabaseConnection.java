package database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Opens connections to the din_nato MySQL database.
 * Every service class will call getConnection() when it needs to run SQL.
 * The URL, username, and password are read from db.properties so the
 * password never appears in the source code.
 */
public class DatabaseConnection {
    private static final String CONFIG_FILE = "db.properties";
    private static final Properties config = new Properties();
    private static String loadError = null;

    // Runs once, the first time this class is used.
    static {
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            config.load(in);
        } catch (IOException e) {
            loadError = "Could not read " + CONFIG_FILE
                    + " in the project folder (" + e.getMessage() + ").";
        }
    }

    // Utility class: nobody should create objects of it.
    private DatabaseConnection() {
    }

    /** Returns a new connection. The caller must close it (use try-with-resources). */
    public static Connection getConnection() throws SQLException {
        if (loadError != null) {
            throw new SQLException(loadError);
        }
        String url = config.getProperty("db.url");
        String user = config.getProperty("db.user");
        String password = config.getProperty("db.password");
        if (url == null || user == null || password == null) {
            throw new SQLException(CONFIG_FILE + " must contain db.url, db.user, and db.password.");
        }
        return DriverManager.getConnection(url, user, password);
    }
}