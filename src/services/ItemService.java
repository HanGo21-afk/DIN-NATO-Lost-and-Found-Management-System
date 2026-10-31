package services;

import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import models.FoundItem;
import models.Item;
import models.LostItem;

/**
 * Handles everything about lost and found items. The data lives in the MySQL
 * "items" table. The GUI calls these methods and never sees any SQL.
 */
public class ItemService {
    public static final String ALL_CATEGORIES = "All Categories";
    public static final String ALL_STATUSES = "All Statuses";
    public static final String[] CATEGORIES = {
        "Electronics", "Wallet / Bag", "Clothing", "Documents / ID",
        "Keys", "Jewelry", "Books / Stationery", "Other"
    };

    private static final String COLUMNS =
            "item_id, item_name, category, description, location, "
            + "date_reported, item_type, status, reported_by";

    // Found-item statuses that only the Claims screen may set.
    private static final List<String> CLAIM_CONTROLLED = Arrays.asList("Pending Claim", "Claimed");

    private static final ItemService INSTANCE = new ItemService();

    public static ItemService getInstance() {
        return INSTANCE;
    }

    private ItemService() {
        seedSampleData();
    }

    /** Adds the sample items, but only when the items table is empty. */
    private void seedSampleData() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM items")) {
            rs.next();
            if (rs.getInt(1) > 0) {
                return;
            }
        } catch (SQLException e) {
            System.err.println("Could not reach the items table: " + e.getMessage());
            return;
        }
        try {
            LocalDate today = LocalDate.now();
            insertItem("Lost", "Black Leather Wallet", "Wallet / Bag", "Contains student ID and a few cards.",
                    "Library, 2nd floor", today.minusDays(6), "Missing", "Maria Santos");
            insertItem("Lost", "iPhone 13 (blue case)", "Electronics", "Blue silicone case, cracked screen protector.",
                    "Cafeteria", today.minusDays(4), "Missing", "Juan Dela Cruz");
            insertItem("Lost", "Dorm Room Keys", "Keys", "Three keys on a red lanyard.",
                    "Gym", today.minusDays(9), "Returned", "Ana Reyes");
            insertItem("Found", "Silver Water Bottle", "Other", "Insulated bottle with a sticker on the side.",
                    "Room 204", today.minusDays(3), "Unclaimed", "Security Desk");
            insertItem("Found", "Prescription Glasses", "Other", "Black frame, in a brown case.",
                    "Main Hallway", today.minusDays(2), "Unclaimed", "Security Desk");
            insertItem("Found", "Scientific Calculator", "Electronics", "Casio calculator with initials 'RB'.",
                    "Computer Lab", today.minusDays(7), "Claimed", "Lab Assistant");
        } catch (RuntimeException e) {
            System.err.println("Could not create the sample items: " + e.getMessage());
        }
    }

    // ----- Status rules (polymorphism: each subclass knows its own statuses) -----

    public String[] statusOptionsFor(String type) {
        return "Lost".equals(type) ? LostItem.STATUSES.clone() : FoundItem.STATUSES.clone();
    }

    public String defaultStatusFor(String type) {
        return "Lost".equals(type) ? "Missing" : "Unclaimed";
    }

    /** Creates a LostItem or FoundItem. The model constructors validate the data. */
    private Item build(int id, String type, String name, String category, String description,
                       String location, LocalDate date, String status, String reportedBy) {
        if ("Lost".equals(type)) {
            return new LostItem(id, name, category, description, location, date, status, reportedBy);
        }
        if ("Found".equals(type)) {
            return new FoundItem(id, name, category, description, location, date, status, reportedBy);
        }
        throw new IllegalArgumentException("Item type must be Lost or Found.");
    }

    private void checkStatus(String type, String status) {
        if (!Arrays.asList(statusOptionsFor(type)).contains(status)) {
            throw new IllegalArgumentException("Invalid status for a " + type.toLowerCase() + " item.");
        }
    }

    /**
     * For found items, Pending Claim and Claimed belong to the Claims screen.
     * Staying on the same status is fine; changing to or from one of them is not.
     */
    private void checkManualStatusChange(String type, String oldStatus, String newStatus) {
        if (!"Found".equals(type)) return;
        if (newStatus.equals(oldStatus)) return;
        if (CLAIM_CONTROLLED.contains(newStatus) || CLAIM_CONTROLLED.contains(oldStatus)) {
            throw new IllegalArgumentException("The statuses Pending Claim and Claimed are managed "
                    + "by the Claims screen. Use New Claim, Approve, or Reject there.");
        }
    }

    /** The table columns have size limits, so check them before saving. */
    private void checkLengths(Item item) {
        if (item.getName().length() > 100) {
            throw new IllegalArgumentException("Item name must be 100 characters or fewer.");
        }
        if (item.getCategory().length() > 50) {
            throw new IllegalArgumentException("Category must be 50 characters or fewer.");
        }
        if (item.getLocation().length() > 150) {
            throw new IllegalArgumentException("Location must be 150 characters or fewer.");
        }
        if (item.getReportedBy().length() > 100) {
            throw new IllegalArgumentException("Reporter name must be 100 characters or fewer.");
        }
    }

    // ----- Create and update -----

    /** Used by the GUI forms. A new found item cannot start as Pending Claim or Claimed. */
    public Item addItem(String type, String name, String category, String description,
                        String location, LocalDate date, String status, String reportedBy) {
        checkStatus(type, status);
        checkManualStatusChange(type, null, status);
        return insertItem(type, name, category, description, location, date, status, reportedBy);
    }

    /** Validates and saves a new item (also used for the sample data). */
    private Item insertItem(String type, String name, String category, String description,
                            String location, LocalDate date, String status, String reportedBy) {
        checkStatus(type, status);
        Item item = build(0, type, name, category, description, location, date, status, reportedBy);
        checkLengths(item);

        String sql = "INSERT INTO items (item_name, category, description, location, "
                + "date_reported, item_type, status, reported_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory());
            ps.setString(3, item.getDescription());
            ps.setString(4, item.getLocation());
            ps.setDate(5, Date.valueOf(item.getDateReported()));
            ps.setString(6, item.getItemType());
            ps.setString(7, item.getStatus());
            ps.setString(8, item.getReportedBy());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                item.setId(keys.getInt(1));
            }
            return item;
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while saving the item: " + e.getMessage(), e);
        }
    }

    public void updateItem(int id, String name, String category, String description,
                           String location, LocalDate date, String status) {
        Item existing = getById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Item not found.");
        }
        checkStatus(existing.getItemType(), status);
        checkManualStatusChange(existing.getItemType(), existing.getStatus(), status);
        Item updated = build(id, existing.getItemType(), name, category, description,
                location, date, status, existing.getReportedBy());
        checkLengths(updated);

        String sql = "UPDATE items SET item_name = ?, category = ?, description = ?, "
                + "location = ?, date_reported = ?, status = ? WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, updated.getName());
            ps.setString(2, updated.getCategory());
            ps.setString(3, updated.getDescription());
            ps.setString(4, updated.getLocation());
            ps.setDate(5, Date.valueOf(updated.getDateReported()));
            ps.setString(6, updated.getStatus());
            ps.setInt(7, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while updating the item: " + e.getMessage(), e);
        }
    }

    public void updateStatus(int id, String status) {
        Item item = getById(id);
        if (item == null) {
            throw new IllegalArgumentException("Item not found.");
        }
        checkStatus(item.getItemType(), status);
        checkManualStatusChange(item.getItemType(), item.getStatus(), status);
        String sql = "UPDATE items SET status = ? WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while updating the status: " + e.getMessage(), e);
        }
    }

    // ----- Reading -----

    public Item getById(int id) {
        String sql = "SELECT " + COLUMNS + " FROM items WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapItem(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while loading the item: " + e.getMessage(), e);
        }
    }

    /** Filters by type, category, and status, and searches several text columns. */
    public List<Item> search(String type, String text, String category, String status) {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM items WHERE 1 = 1");
        List<String> params = new ArrayList<>();

        if (type != null) {
            sql.append(" AND item_type = ?");
            params.add(type);
        }
        if (category != null && !category.isEmpty() && !ALL_CATEGORIES.equals(category)) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        if (status != null && !status.isEmpty() && !ALL_STATUSES.equals(status)) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        String q = text == null ? "" : text.trim();
        if (!q.isEmpty()) {
            sql.append(" AND (item_name LIKE ? OR category LIKE ? OR description LIKE ?"
                    + " OR location LIKE ? OR reported_by LIKE ?)");
            String pattern = likePattern(q);
            for (int i = 0; i < 5; i++) {
                params.add(pattern);
            }
        }
        sql.append(" ORDER BY item_id");

        List<Item> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapItem(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while searching items: " + e.getMessage(), e);
        }
        return result;
    }

    public List<Item> recent(int limit) {
        String sql = "SELECT " + COLUMNS + " FROM items "
                + "ORDER BY date_reported DESC, item_id DESC LIMIT ?";
        List<Item> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapItem(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while loading recent items: " + e.getMessage(), e);
        }
        return result;
    }

    // ----- Dashboard counts -----

    public int count(String type) {
        String sql = "SELECT COUNT(*) FROM items WHERE item_type = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while counting items: " + e.getMessage(), e);
        }
    }

    /** Counts returned lost items and claimed found items (same rule as isResolved()). */
    public int countResolved() {
        String sql = "SELECT COUNT(*) FROM items WHERE "
                + "(item_type = 'Lost' AND status = 'Returned') "
                + "OR (item_type = 'Found' AND status = 'Claimed')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while counting items: " + e.getMessage(), e);
        }
    }

    // ----- Helpers -----

    /** Turns one database row into a LostItem or FoundItem object. */
    private Item mapItem(ResultSet rs) throws SQLException {
        return build(rs.getInt("item_id"), rs.getString("item_type"), rs.getString("item_name"),
                rs.getString("category"), rs.getString("description"), rs.getString("location"),
                rs.getDate("date_reported").toLocalDate(), rs.getString("status"),
                rs.getString("reported_by"));
    }

    /** Wraps the search text for LIKE, and escapes the characters that LIKE treats specially. */
    private static String likePattern(String text) {
        String escaped = text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }
}