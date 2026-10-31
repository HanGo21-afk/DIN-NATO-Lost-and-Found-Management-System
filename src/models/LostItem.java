package models;

import java.time.LocalDate;


public class LostItem extends Item {
    public static final String[] STATUSES = {"Missing", "Match Found", "Returned"};

    public LostItem(int id, String name, String category, String description,
                    String location, LocalDate dateReported, String status, String reportedBy) {
        super(id, name, category, description, location, dateReported, status, reportedBy);
    }

    @Override
    public String getItemType() {
        return "Lost";
    }

    @Override
    public String[] getStatusOptions() {
        return STATUSES.clone();
    }

    @Override
    public String getDefaultStatus() {
        return "Missing";
    }

    @Override
    public boolean isResolved() {
        return "Returned".equals(getStatus());
    }

    @Override
    public String describe() {
        return "LOST ITEM REPORT\n"
                + "------------------------------\n"
                + "Item: " + getName() + "\n"
                + "Category: " + getCategory() + "\n"
                + "Last seen at: " + getLocation() + "\n"
                + "Date lost: " + getDateReported() + "\n"
                + "Reported by: " + getReportedBy() + "\n"
                + "Status: " + getStatus() + "\n\n"
                + "Description:\n" + (getDescription().isEmpty() ? "(none)" : getDescription());
    }
}
