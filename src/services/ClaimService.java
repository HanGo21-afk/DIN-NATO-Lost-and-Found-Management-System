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
import java.util.List;
import models.Claim;
import models.Item;

/**
 * Handles claims on found items. Claims live in the MySQL "claims" table.
 * Creating, approving, and rejecting a claim each change more than one row,
 * so each of them runs as one transaction: all changes are saved together,
 * or none of them are.
 */
public class ClaimService {
    private static final ClaimService INSTANCE = new ClaimService();

    public static ClaimService getInstance() {
        return INSTANCE;
    }

    private static final String COLUMNS =
            "claim_id, item_id, claimant_name, claim_date, status, remarks";

    private final ItemService itemService = ItemService.getInstance();

    private ClaimService() {
    }

    // ----- Reading -----

    /** All claims, newest first. */
    public List<Claim> getAllClaims() {
        String sql = "SELECT " + COLUMNS + " FROM claims ORDER BY claim_id DESC";
        List<Claim> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapClaim(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while loading claims: " + e.getMessage(), e);
        }
        return result;
    }

    /** Found items that nobody has finished claiming yet. */
    public List<Item> getClaimableItems() {
        List<Item> result = new ArrayList<>();
        for (Item item : itemService.search("Found", "", ItemService.ALL_CATEGORIES, ItemService.ALL_STATUSES)) {
            if (!item.isResolved()) result.add(item);
        }
        return result;
    }

    // ----- Creating a claim -----

    public Claim createClaim(int itemId, String claimantName, String remarks) {
        Item item = itemService.getById(itemId);
        if (item == null || !"Found".equals(item.getItemType())) {
            throw new IllegalArgumentException("Claims can only be made on found items.");
        }
        if (item.isResolved()) {
            throw new IllegalArgumentException("This item has already been claimed.");
        }
        // Creating the Claim object runs its validation (claimant name is required).
        LocalDate today = LocalDate.now();
        Claim claim = new Claim(0, itemId, claimantName, today, "Pending", remarks);
        if (claim.getClaimantName().length() > 100) {
            throw new IllegalArgumentException("Claimant name must be 100 characters or fewer.");
        }

        String sql = "INSERT INTO claims (item_id, claimant_name, claim_date, status, remarks) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int newId;
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, itemId);
                    ps.setString(2, claim.getClaimantName());
                    ps.setDate(3, Date.valueOf(today));
                    ps.setString(4, "Pending");
                    ps.setString(5, claim.getRemarks());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        newId = keys.getInt(1);
                    }
                }
                setItemStatus(conn, itemId, "Pending Claim");
                conn.commit();
                return new Claim(newId, itemId, claim.getClaimantName(), today, "Pending", claim.getRemarks());
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while saving the claim: " + e.getMessage(), e);
        }
    }

    // ----- Deciding a claim -----

    public void approve(int claimId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Claim claim = requirePending(conn, claimId);
                setClaimStatus(conn, claimId, "Approved");
                setItemStatus(conn, claim.getItemId(), "Claimed");
                // Other waiting claims on the same item can no longer be approved.
                String sql = "UPDATE claims SET status = 'Rejected' "
                        + "WHERE item_id = ? AND claim_id <> ? AND status = 'Pending'";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, claim.getItemId());
                    ps.setInt(2, claimId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while approving the claim: " + e.getMessage(), e);
        }
    }

    public void reject(int claimId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Claim claim = requirePending(conn, claimId);
                setClaimStatus(conn, claimId, "Rejected");
                // If nobody else is waiting, the item goes back to Unclaimed.
                if (!hasPendingClaim(conn, claim.getItemId())) {
                    setItemStatus(conn, claim.getItemId(), "Unclaimed");
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while rejecting the claim: " + e.getMessage(), e);
        }
    }

    // ----- Helpers (all use the connection of the running transaction) -----

    /** Loads a claim and makes sure it is still pending. */
    private Claim requirePending(Connection conn, int claimId) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM claims WHERE claim_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, claimId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Claim not found.");
                }
                Claim claim = mapClaim(rs);
                if (!"Pending".equals(claim.getStatus())) {
                    throw new IllegalArgumentException("Only pending claims can be decided.");
                }
                return claim;
            }
        }
    }

    private void setClaimStatus(Connection conn, int claimId, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE claims SET status = ? WHERE claim_id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, claimId);
            ps.executeUpdate();
        }
    }

    // The item status is updated here, on the same connection, so it is part of the transaction.
    private void setItemStatus(Connection conn, int itemId, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE items SET status = ? WHERE item_id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, itemId);
            ps.executeUpdate();
        }
    }

    private boolean hasPendingClaim(Connection conn, int itemId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM claims WHERE item_id = ? AND status = 'Pending'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** Turns one database row into a Claim object. */
    private Claim mapClaim(ResultSet rs) throws SQLException {
        return new Claim(
                rs.getInt("claim_id"),
                rs.getInt("item_id"),
                rs.getString("claimant_name"),
                rs.getDate("claim_date").toLocalDate(),
                rs.getString("status"),
                rs.getString("remarks"));
    }
}