package services;

import database.DatabaseConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import models.User;

/**
 * Handles everything about user accounts. The data now lives in the MySQL
 * "users" table. The GUI calls these methods and never sees any SQL.
 */
public class UserService {
    private static final UserService INSTANCE = new UserService();

    public static UserService getInstance() {
        return INSTANCE;
    }

    private static final String COLUMNS =
            "user_id, full_name, username, password_hash, role, created_at";

    private UserService() {
        seedStarterAccounts();
    }

    /** Adds the two starter accounts, but only when the users table is empty. */
    private void seedStarterAccounts() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            if (rs.getInt(1) > 0) {
                return;
            }
        } catch (SQLException e) {
            System.err.println("Could not reach the users table: " + e.getMessage());
            return;
        }
        try {
            addUser("System Administrator", "admin", "admin123", "Admin");
            addUser("Student Staff", "student", "student123", "Staff");
        } catch (RuntimeException e) {
            System.err.println("Could not create the starter accounts: " + e.getMessage());
        }
    }

    /** Validates the input, saves a new user to MySQL, and returns it. */
    public User addUser(String fullName, String username, String password, String role) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        // Creating a User object runs its validation (name, username, role).
        User checked = new User(0, fullName, username, "", role, LocalDateTime.now());
        if (checked.getFullName().length() > 100) {
            throw new IllegalArgumentException("Full name must be 100 characters or fewer.");
        }
        if (checked.getUsername().length() > 50) {
            throw new IllegalArgumentException("Username must be 50 characters or fewer.");
        }
        if (findByUsername(checked.getUsername()) != null) {
            throw new IllegalArgumentException("That username is already taken.");
        }

        String hash = createHash(password);
        LocalDateTime now = LocalDateTime.now();
        String sql = "INSERT INTO users (full_name, username, password_hash, role, created_at) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, checked.getFullName());
            ps.setString(2, checked.getUsername());
            ps.setString(3, hash);
            ps.setString(4, checked.getRole());
            ps.setTimestamp(5, Timestamp.valueOf(now));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new User(keys.getInt(1), checked.getFullName(), checked.getUsername(),
                        hash, checked.getRole(), now);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("That username is already taken.");
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while adding the user: " + e.getMessage(), e);
        }
    }

    /** Returns the user if the username and password are correct, otherwise null. */
    public User authenticate(String username, String password) {
        if (username == null || password == null) return null;
        User user = findByUsername(username.trim());
        if (user == null) return null;
        return verify(password, user.getPasswordHash()) ? user : null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT " + COLUMNS + " FROM users ORDER BY user_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while loading users: " + e.getMessage(), e);
        }
        return users;
    }

    private User findByUsername(String username) {
        String sql = "SELECT " + COLUMNS + " FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while finding the user: " + e.getMessage(), e);
        }
    }

    /** Turns one database row into a User object. */
    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("full_name"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("role"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }

    // ----- Password hashing (unchanged: salted SHA-256, stored as "salt:hash") -----

    private static String createHash(String password) {
        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);
        return salt + ":" + hash(password, salt);
    }

    private static boolean verify(String password, String stored) {
        String[] parts = stored.split(":", 2);
        return parts.length == 2 && hash(password, parts[0]).equals(parts[1]);
    }

    private static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(md.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }
}