package gui;

import models.User;


public class LostItemsPanel extends ItemsPanel {
    public LostItemsPanel(User user, Runnable onChange) {
        super("Lost", "Lost Items", "Items that people have reported missing.",
                "Report Lost Item", user, onChange);
    }
}
