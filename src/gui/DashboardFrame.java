package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import models.Item;
import models.User;
import services.ItemService;
import utils.UIStyles;


public class DashboardFrame extends JFrame {
    private final User user;
    private final ItemService itemService = ItemService.getInstance();

    private final CardLayout cards = new CardLayout();
    private final JPanel contentArea = new JPanel(cards);
    private final Map<String, UIStyles.RoundedButton> navButtons = new LinkedHashMap<>();

    private DashboardHome home;
    private ItemsPanel lostPanel;
    private ItemsPanel foundPanel;
    private ClaimsPanel claimsPanel;
    private UsersPanel usersPanel;

    public DashboardFrame(User user) {
        this.user = user;
        setTitle("DIN NATO - Loss and Found Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 740);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);

        home = new DashboardHome();
        Runnable onChange = home::refresh;
        lostPanel = new LostItemsPanel(user, onChange);
        foundPanel = new FoundItemsPanel(user, onChange);
        claimsPanel = new ClaimsPanel(user, onChange);

        contentArea.setBackground(UIStyles.WHITE);
        contentArea.add(home, "dashboard");
        contentArea.add(lostPanel, "lost");
        contentArea.add(foundPanel, "found");
        contentArea.add(claimsPanel, "claims");
        if (user.isAdmin()) {
            usersPanel = new UsersPanel();
            contentArea.add(usersPanel, "users");
        }

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(contentArea, BorderLayout.CENTER);
        setContentPane(root);

        showPage("dashboard");
    }

    

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UIStyles.WHITE);
        bar.setBorder(new javax.swing.border.CompoundBorder(
                new MatteBorder(0, 0, 1, 0, UIStyles.BORDER), new EmptyBorder(14, 24, 14, 24)));

        JLabel brand = new JLabel("DIN NATO");
        brand.setFont(UIStyles.FONT_HEADING.deriveFont(Font.BOLD, 18f));
        brand.setForeground(UIStyles.PRIMARY);
        JLabel tag = UIStyles.subtitle("  Loss and Found Management");

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        left.add(brand);
        left.add(tag);

        JLabel account = new JLabel(user.getFullName() + "  |  " + user.getRole());
        account.setFont(UIStyles.FONT_BOLD);
        account.setForeground(UIStyles.TEXT);

        bar.add(left, BorderLayout.WEST);
        bar.add(account, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UIStyles.NAVY);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(20, 14, 20, 14));

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));

        addNav(nav, "dashboard", "Dashboard");
        addNav(nav, "lost", "Lost Items");
        addNav(nav, "found", "Found Items");
        addNav(nav, "claims", "Claims");
        if (user.isAdmin()) {
            addNav(nav, "users", "Users");
        }

        UIStyles.RoundedButton logout = UIStyles.navButton("Logout");
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        logout.addActionListener(e -> logout());

        sidebar.add(nav, BorderLayout.NORTH);
        sidebar.add(logout, BorderLayout.SOUTH);
        return sidebar;
    }

    private void addNav(JPanel nav, String key, String label) {
        UIStyles.RoundedButton button = UIStyles.navButton(label);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.addActionListener(e -> showPage(key));
        navButtons.put(key, button);
        nav.add(button);
        nav.add(javax.swing.Box.createVerticalStrut(6));
    }

    
    private void showPage(String key) {
        cards.show(contentArea, key);
        for (Map.Entry<String, UIStyles.RoundedButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(key);
            entry.getValue().setColors(
                    active ? UIStyles.PRIMARY : UIStyles.NAVY,
                    active ? UIStyles.PRIMARY_HOVER : UIStyles.NAVY_HOVER);
        }
        switch (key) {
            case "dashboard": home.refresh(); break;
            case "lost": lostPanel.refresh(); break;
            case "found": foundPanel.refresh(); break;
            case "claims": claimsPanel.refresh(); break;
            case "users": if (usersPanel != null) usersPanel.refresh(); break;
            default: break;
        }
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this, "Do you want to log out?", "Logout",
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    private void openForm(String type) {
        ItemFormDialog dialog = new ItemFormDialog(this, type, null, user.getFullName());
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            home.refresh();
        }
    }

    

    private class DashboardHome extends JPanel {
        private final JLabel lostValue = new JLabel("0");
        private final JLabel foundValue = new JLabel("0");
        private final JLabel claimedValue = new JLabel("0");
        private final DefaultTableModel model;

        DashboardHome() {
            setLayout(new BorderLayout(0, 22));
            setBackground(UIStyles.WHITE);
            setBorder(new EmptyBorder(28, 32, 24, 32));

            
            JPanel titles = new JPanel(new BorderLayout(0, 4));
            titles.setOpaque(false);
            titles.add(UIStyles.title("Dashboard"), BorderLayout.NORTH);
            titles.add(UIStyles.subtitle("Welcome back, " + user.getFullName()
                    + ". Here is what is happening today."), BorderLayout.CENTER);

            UIStyles.RoundedButton reportButton = UIStyles.primaryButton("+ Report Item");
            reportButton.addActionListener(e -> {
                JPopupMenu menu = new JPopupMenu();
                JMenuItem lost = new JMenuItem("Report Lost Item");
                JMenuItem found = new JMenuItem("Register Found Item");
                lost.addActionListener(ev -> openForm("Lost"));
                found.addActionListener(ev -> openForm("Found"));
                menu.add(lost);
                menu.add(found);
                menu.show(reportButton, 0, reportButton.getHeight());
            });

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.add(titles, BorderLayout.WEST);
            header.add(reportButton, BorderLayout.EAST);
            add(header, BorderLayout.NORTH);

            
            JPanel cardRow = new JPanel(new GridLayout(1, 3, 16, 0));
            cardRow.setOpaque(false);
            cardRow.setPreferredSize(new Dimension(0, 110));
            cardRow.add(buildCard("Lost Items", lostValue, UIStyles.PRIMARY));
            cardRow.add(buildCard("Found Items", foundValue, UIStyles.WARNING));
            cardRow.add(buildCard("Claimed / Returned", claimedValue, UIStyles.SUCCESS));

            // Recent items table
            model = new DefaultTableModel(
                    new String[]{"ID", "Item", "Type", "Category", "Location", "Date", "Status"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable table = new JTable(model);
            UIStyles.styleTable(table);
            table.getColumnModel().getColumn(6).setCellRenderer(new UIStyles.StatusCellRenderer());
            int[] widths = {50, 200, 80, 130, 160, 100, 110};
            for (int i = 0; i < widths.length; i++) {
                table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }

            JPanel recent = new JPanel(new BorderLayout(0, 10));
            recent.setOpaque(false);
            recent.add(UIStyles.heading("Recent Items"), BorderLayout.NORTH);
            recent.add(UIStyles.tableScroll(table), BorderLayout.CENTER);

            JPanel center = new JPanel(new BorderLayout(0, 22));
            center.setOpaque(false);
            center.add(cardRow, BorderLayout.NORTH);
            center.add(recent, BorderLayout.CENTER);
            add(center, BorderLayout.CENTER);

            refresh();
        }

        private JPanel buildCard(String title, JLabel valueLabel, Color accent) {
            UIStyles.RoundedPanel card = new UIStyles.RoundedPanel(14, UIStyles.LIGHT_GRAY, UIStyles.BORDER);
            card.setLayout(new BorderLayout());
            card.setBorder(new EmptyBorder(14, 20, 14, 20));
            valueLabel.setFont(UIStyles.FONT_TITLE.deriveFont(36f));
            valueLabel.setForeground(accent);
            JLabel label = new JLabel(title);
            label.setFont(UIStyles.FONT_BOLD);
            label.setForeground(UIStyles.TEXT_MUTED);
            card.add(label, BorderLayout.NORTH);
            card.add(valueLabel, BorderLayout.CENTER);
            return card;
        }

        void refresh() {
            lostValue.setText(String.valueOf(itemService.count("Lost")));
            foundValue.setText(String.valueOf(itemService.count("Found")));
            claimedValue.setText(String.valueOf(itemService.countResolved()));
            model.setRowCount(0);
            // Each element is an Item reference, but the real object is a LostItem or FoundItem.
            for (Item item : itemService.recent(8)) {
                model.addRow(new Object[]{
                    item.getId(), item.getName(), item.getItemType(), item.getCategory(),
                    item.getLocation(), item.getDateReported(), item.getStatus()
                });
            }
        }
    }
}
