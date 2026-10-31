package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Temporary test: run this file by itself to check the database connection.
 * It does not start the GUI.
 */
public class TestConnection {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM items")) {
            rs.next();
            System.out.println("SUCCESS: connected to din_nato. Rows in items table: " + rs.getInt(1));
        } catch (SQLException e) {
            System.out.println("FAILED: " + e.getMessage());
        }
    }
}