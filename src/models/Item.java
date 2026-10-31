package models;

import java.time.LocalDate;


public abstract class Item {
    private int id;
    private String name;
    private String category;
    private String description;
    private String location;
    private LocalDate dateReported;
    private String status;
    private String reportedBy;

    
    public Item(int id, String name, String category, String description,
                String location, LocalDate dateReported, String status, String reportedBy) {
        this.id = id;
        setName(name);
        setCategory(category);
        setDescription(description);
        setLocation(location);
        setDateReported(dateReported);
        setStatus(status);
        this.reportedBy = reportedBy == null ? "" : reportedBy;
    }

    
    public abstract String getItemType();
    public abstract String[] getStatusOptions();
    public abstract String getDefaultStatus();
    public abstract boolean isResolved();
    public abstract String describe();

    
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public LocalDate getDateReported() { return dateReported; }
    public String getStatus() { return status; }
    public String getReportedBy() { return reportedBy; }

    
    public void setId(int id) { this.id = id; }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Item name is required.");
        this.name = name.trim();
    }

    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty())
            throw new IllegalArgumentException("Category is required.");
        this.category = category.trim();
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description.trim();
    }

    public void setLocation(String location) {
        if (location == null || location.trim().isEmpty())
            throw new IllegalArgumentException("Location is required.");
        this.location = location.trim();
    }

    public void setDateReported(LocalDate dateReported) {
        if (dateReported == null)
            throw new IllegalArgumentException("Date is required.");
        if (dateReported.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Date cannot be in the future.");
        this.dateReported = dateReported;
    }

    public void setStatus(String status) {
        if (status == null || status.trim().isEmpty())
            throw new IllegalArgumentException("Status is required.");
        this.status = status.trim();
    }

    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }

    @Override
    public String toString() {
        return "#" + id + " - " + name;
    }
}
