package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import models.Item;
import services.ItemService;
import utils.UIStyles;


public class ItemFormDialog extends JDialog {
    private final ItemService service = ItemService.getInstance();
    private final String type;
    private final Item existing;
    private final String reporter;
    private boolean saved = false;

    private final JTextField nameField = new JTextField();
    private final JComboBox<String> categoryBox = new JComboBox<>(ItemService.CATEGORIES);
    private final JTextArea descriptionArea = new JTextArea(4, 20);
    private final JTextField locationField = new JTextField();
    private final JTextField dateField = new JTextField();
    private final JTextField typeField = new JTextField();
    private final JComboBox<String> statusBox;
    private final JLabel errorLabel = new JLabel(" ");

    public ItemFormDialog(Window owner, String type, Item existing, String reporter) {
        super(owner, titleFor(type, existing), ModalityType.APPLICATION_MODAL);
        this.type = type;
        this.existing = existing;
        this.reporter = reporter;
        this.statusBox = new JComboBox<>(service.statusOptionsFor(type));

        buildForm();
        fillFields();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(owner);
    }

    private static String titleFor(String type, Item existing) {
        if (existing != null) return "Edit " + type + " Item";
        return "Lost".equals(type) ? "Report Lost Item" : "Register Found Item";
    }

    public boolean isSaved() {
        return saved;
    }

    private void buildForm() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(UIStyles.WHITE);
        root.setBorder(new EmptyBorder(22, 26, 20, 26));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(UIStyles.heading(getTitle()), BorderLayout.NORTH);
        root.add(head, BorderLayout.NORTH);

        UIStyles.styleTextField(nameField);
        UIStyles.styleTextField(descriptionArea);
        UIStyles.styleTextField(locationField);
        UIStyles.styleTextField(dateField);
        UIStyles.styleTextField(typeField);
        typeField.setEditable(false);
        typeField.setBackground(UIStyles.LIGHT_GRAY);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        UIStyles.styleCombo(categoryBox);
        UIStyles.styleCombo(statusBox);

        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(null);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(380, 480));
        addRow(form, 0, "Item Name *", nameField);
        addRow(form, 1, "Category *", categoryBox);
        addRow(form, 2, "Description", descScroll);
        addRow(form, 3, "Location *", locationField);
        addRow(form, 4, "Date Reported * (yyyy-MM-dd)", dateField);
        addRow(form, 5, "Item Type", typeField);
        addRow(form, 6, "Status", statusBox);
        root.add(form, BorderLayout.CENTER);

        errorLabel.setFont(UIStyles.FONT_SMALL);
        errorLabel.setForeground(UIStyles.ERROR);

        UIStyles.RoundedButton save = UIStyles.primaryButton("Save");
        UIStyles.RoundedButton cancel = UIStyles.secondaryButton("Cancel");
        save.addActionListener(e -> onSave());
        cancel.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(save);

        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setOpaque(false);
        south.add(errorLabel, BorderLayout.NORTH);
        south.add(buttons, BorderLayout.SOUTH);
        root.add(south, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(save);
        setContentPane(root);
    }

    private void addRow(JPanel panel, int row, String label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;

        c.gridy = row * 2;
        c.insets = new Insets(row == 0 ? 0 : 10, 0, 4, 0);
        panel.add(UIStyles.fieldLabel(label), c);

        c.gridy = row * 2 + 1;
        c.insets = new Insets(0, 0, 0, 0);
        if (field instanceof JScrollPane) {
            field.setBorder(new LineBorder(UIStyles.BORDER, 1, true));
        }
        panel.add(field, c);
    }

    private void fillFields() {
        typeField.setText(type);
        if (existing == null) {
            dateField.setText(LocalDate.now().toString());
            statusBox.setSelectedItem(service.defaultStatusFor(type));
        } else {
            nameField.setText(existing.getName());
            categoryBox.setSelectedItem(existing.getCategory());
            descriptionArea.setText(existing.getDescription());
            locationField.setText(existing.getLocation());
            dateField.setText(existing.getDateReported().toString());
            statusBox.setSelectedItem(existing.getStatus());
        }
    }

    private void onSave() {
        try {
            LocalDate date = LocalDate.parse(dateField.getText().trim());
            String category = (String) categoryBox.getSelectedItem();
            String status = (String) statusBox.getSelectedItem();

            if (existing == null) {
                service.addItem(type, nameField.getText(), category, descriptionArea.getText(),
                        locationField.getText(), date, status, reporter);
            } else {
                service.updateItem(existing.getId(), nameField.getText(), category,
                        descriptionArea.getText(), locationField.getText(), date, status);
            }
            saved = true;
            dispose();
        } catch (DateTimeParseException ex) {
            errorLabel.setText("Date must use the format yyyy-MM-dd (example: 2026-10-22).");
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
        }
    }
}
