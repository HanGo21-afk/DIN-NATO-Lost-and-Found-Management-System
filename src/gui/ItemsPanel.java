package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import models.Item;
import models.User;
import services.ItemService;
import utils.UIStyles;


public class ItemsPanel extends JPanel {
    private final ItemService itemService = ItemService.getInstance();
    private final String type;
    private final User user;
    private final Runnable onChange;

    private final JTextField searchField = new JTextField(22);
    private final JComboBox<String> categoryBox;
    private final JComboBox<String> statusBox;
    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel feedback = new JLabel(" ");
    private List<Item> shown = new ArrayList<>();

    public ItemsPanel(String type, String title, String subtitle, String addLabel,
                      User user, Runnable onChange) {
        this.type = type;
        this.user = user;
        this.onChange = onChange;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIStyles.WHITE);
        setBorder(new EmptyBorder(28, 32, 24, 32));

        
        JPanel titles = new JPanel(new BorderLayout(0, 4));
        titles.setOpaque(false);
        titles.add(UIStyles.title(title), BorderLayout.NORTH);
        titles.add(UIStyles.subtitle(subtitle), BorderLayout.CENTER);

        UIStyles.RoundedButton addButton = UIStyles.primaryButton("+ " + addLabel);
        addButton.addActionListener(e -> onAdd());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(titles, BorderLayout.WEST);
        header.add(addButton, BorderLayout.EAST);

        
        String[] categories = new String[ItemService.CATEGORIES.length + 1];
        categories[0] = ItemService.ALL_CATEGORIES;
        System.arraycopy(ItemService.CATEGORIES, 0, categories, 1, ItemService.CATEGORIES.length);
        categoryBox = new JComboBox<>(categories);

        String[] options = itemService.statusOptionsFor(type);
        String[] statuses = new String[options.length + 1];
        statuses[0] = ItemService.ALL_STATUSES;
        System.arraycopy(options, 0, statuses, 1, options.length);
        statusBox = new JComboBox<>(statuses);

        UIStyles.styleTextField(searchField);
        searchField.setToolTipText("Search by name, description, location...");
        UIStyles.styleCombo(categoryBox);
        UIStyles.styleCombo(statusBox);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);
        toolbar.add(UIStyles.fieldLabel("Search"));
        toolbar.add(searchField);
        toolbar.add(categoryBox);
        toolbar.add(statusBox);

        JPanel north = new JPanel(new BorderLayout(0, 16));
        north.setOpaque(false);
        north.add(header, BorderLayout.NORTH);
        north.add(toolbar, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        
        model = new DefaultTableModel(
                new String[]{"ID", "Item Name", "Category", "Location", "Date", "Status", "Reported By"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        UIStyles.styleTable(table);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(5).setCellRenderer(new UIStyles.StatusCellRenderer());
        int[] widths = {50, 200, 130, 160, 100, 110, 130};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) onView();
            }
        });
        JScrollPane scroll = UIStyles.tableScroll(table);
        add(scroll, BorderLayout.CENTER);

        // ----- Actions -----
        UIStyles.RoundedButton editButton = UIStyles.secondaryButton("Edit");
        UIStyles.RoundedButton viewButton = UIStyles.secondaryButton("View");
        UIStyles.RoundedButton statusButton = UIStyles.secondaryButton("Update Status");
        editButton.addActionListener(e -> onEdit());
        viewButton.addActionListener(e -> onView());
        statusButton.addActionListener(e -> onUpdateStatus());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.add(editButton);
        actions.add(viewButton);
        actions.add(statusButton);

        feedback.setFont(UIStyles.FONT_BOLD);
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(actions, BorderLayout.WEST);
        south.add(feedback, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);

        
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { refresh(); }

            @Override
            public void removeUpdate(DocumentEvent e) { refresh(); }

            @Override
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });
        categoryBox.addActionListener(e -> refresh());
        statusBox.addActionListener(e -> refresh());

        refresh();
    }

    
    public void refresh() {
        shown = itemService.search(type, searchField.getText(),
                (String) categoryBox.getSelectedItem(), (String) statusBox.getSelectedItem());
        model.setRowCount(0);
        for (Item item : shown) {
            model.addRow(new Object[]{
                item.getId(), item.getName(), item.getCategory(), item.getLocation(),
                item.getDateReported(), item.getStatus(), item.getReportedBy()
            });
        }
    }

    private Item selectedItem() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= shown.size()) {
            JOptionPane.showMessageDialog(this, "Please select a record in the table first.",
                    "No selection", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return shown.get(row);
    }

    private Window owner() {
        return SwingUtilities.getWindowAncestor(this);
    }

    private void showFeedback(String message) {
        feedback.setForeground(UIStyles.SUCCESS);
        feedback.setText(message);
    }

    private void onAdd() {
        ItemFormDialog dialog = new ItemFormDialog(owner(), type, null, user.getFullName());
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refresh();
            onChange.run();
            showFeedback("Record saved successfully.");
        }
    }

    private void onEdit() {
        Item item = selectedItem();
        if (item == null) return;
        ItemFormDialog dialog = new ItemFormDialog(owner(), type, item, user.getFullName());
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refresh();
            onChange.run();
            showFeedback("Record updated successfully.");
        }
    }

    private void onView() {
        Item item = selectedItem();
        if (item == null) return;
        
        JTextArea text = new JTextArea(item.describe());
        text.setEditable(false);
        text.setFont(UIStyles.FONT_BODY);
        text.setForeground(UIStyles.TEXT);
        text.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        JScrollPane scroll = new JScrollPane(text);
        scroll.setPreferredSize(new java.awt.Dimension(380, 260));
        JOptionPane.showMessageDialog(this, scroll, item.getItemType() + " Item #" + item.getId(),
                JOptionPane.PLAIN_MESSAGE);
    }

    private void onUpdateStatus() {
        Item item = selectedItem();
        if (item == null) return;
        
        String choice = (String) JOptionPane.showInputDialog(this,
                "Choose a new status for \"" + item.getName() + "\":", "Update Status",
                JOptionPane.PLAIN_MESSAGE, null, item.getStatusOptions(), item.getStatus());
        if (choice == null) return;
        try {
            itemService.updateStatus(item.getId(), choice);
            refresh();
            onChange.run();
            showFeedback("Status changed to " + choice + ".");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not update status",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
