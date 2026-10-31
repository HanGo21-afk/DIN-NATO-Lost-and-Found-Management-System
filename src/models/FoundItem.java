package models;

import java.time.LocalDate;


public class FoundItem extends Item {
    public static final String[] STATUSES = {"Unclaimed", "Pending Claim", "Claimed"};

    public FoundItem(int id, String name, String category, String description,
                     String location, LocalDate dateReported, String status, String reportedBy) {
        super(id, name, category, description, location, dateReported, status, reportedBy);
    }

    @Override
    public String getItemType() {
        return "Found";
    }

    @Override
    public String[] getStatusOptions() {
        return STATUSES.clone();
    }

    @Override
    public String getDefaultStatus() {
        return "Unclaimed";
    }

    @Override
    public boolean isResolved() {
        return "Claimed".equals(getStatus());
    }

    @Override
    public String describe() {
        return "FOUND ITEM RECORD\n"
                + "------------------------------\n"
                + "Item: " + getName() + "\n"
                + "Category: " + getCategory() + "\n"
                + "Found at: " + getLocation() + "\n"
                + "Date found: " + getDateReported() + "\n"
                + "Turned in by: " + getReportedBy() + "\n"
                + "Status: " + getStatus() + "\n\n"
                + "Description:\n" + (getDescription().isEmpty() ? "(none)" : getDescription())
                + "\n\nKept safe until the rightful owner claims it.";
    }
}
