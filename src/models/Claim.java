package models;

import java.time.LocalDate;
import java.util.Arrays;


public class Claim {
    public static final String[] STATUSES = {"Pending", "Approved", "Rejected"};

    private int id;
    private int itemId;
    private String claimantName;
    private LocalDate claimDate;
    private String status;
    private String remarks;

    public Claim(int id, int itemId, String claimantName, LocalDate claimDate,
                 String status, String remarks) {
        this.id = id;
        this.itemId = itemId;
        setClaimantName(claimantName);
        this.claimDate = claimDate;
        setStatus(status);
        this.remarks = remarks == null ? "" : remarks.trim();
    }

    public int getId() { return id; }
    public int getItemId() { return itemId; }
    public String getClaimantName() { return claimantName; }
    public LocalDate getClaimDate() { return claimDate; }
    public String getStatus() { return status; }
    public String getRemarks() { return remarks; }

    public void setClaimantName(String claimantName) {
        if (claimantName == null || claimantName.trim().isEmpty())
            throw new IllegalArgumentException("Claimant name is required.");
        this.claimantName = claimantName.trim();
    }

    public void setStatus(String status) {
        if (!Arrays.asList(STATUSES).contains(status))
            throw new IllegalArgumentException("Invalid claim status.");
        this.status = status;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks == null ? "" : remarks.trim();
    }
}
