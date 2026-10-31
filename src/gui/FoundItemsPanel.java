package gui;

import models.User;


public class FoundItemsPanel extends ItemsPanel {
    public FoundItemsPanel(User user, Runnable onChange) {
        super("Found", "Found Items", "Items that have been turned in and are waiting for their owners.",
                "Register Found Item", user, onChange);
    }
}
