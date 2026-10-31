package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import models.User;
import services.UserService;
import utils.UIStyles;


public class UsersPanel extends JPanel {
    private final UserService userService = UserService.getInstance();
    private final DefaultTableModel model;
    private final JLabel feedback = new JLabel(" ");

    public UsersPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIStyles.WHITE);
        setBorder(new EmptyBorder(28, 32, 24, 32));

        JPanel titles = new JPanel(new BorderLayout(0, 4));
        titles.setOpaque(false);
        titles.add(UIStyles.title("Users"), BorderLayout.NORTH);
        titles.add(UIStyles.subtitle("People who can sign in to DIN NATO."), BorderLayout.CENTER);

        UIStyles.RoundedButton addUser = UIStyles.primaryButton("+ Add User");
        addUser.addActionListener(e -> onAddUser());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(titles, BorderLayout.WEST);
        header.add(addUser, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"ID", "Full Name", "Username", "Role", "Created"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        UIStyles.styleTable(table);
        add(UIStyles.tableScroll(table), BorderLayout.CENTER);

        feedback.setFont(UIStyles.FONT_BOLD);
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(feedback, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        model.setRowCount(0);
        for (User u : userService.getAllUsers()) {
            model.addRow(new Object[]{
                u.getId(), u.getFullName(), u.getUsername(), u.getRole(), u.getCreatedAt().format(format)
            });
        }
    }

    private void onAddUser() {
        JTextField nameField = new JTextField(20);
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Staff", "Admin"});
        UIStyles.styleTextField(nameField);
        UIStyles.styleTextField(usernameField);
        UIStyles.styleTextField(passwordField);
        UIStyles.styleCombo(roleBox);

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
        form.add(UIStyles.fieldLabel("Full name"));
        form.add(nameField);
        form.add(UIStyles.fieldLabel("Username"));
        form.add(usernameField);
        form.add(UIStyles.fieldLabel("Password (at least 6 characters)"));
        form.add(passwordField);
        form.add(UIStyles.fieldLabel("Role"));
        form.add(roleBox);

        int result = JOptionPane.showConfirmDialog(this, form, "Add User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            userService.addUser(nameField.getText(), usernameField.getText(),
                    new String(passwordField.getPassword()), (String) roleBox.getSelectedItem());
            refresh();
            feedback.setForeground(UIStyles.SUCCESS);
            feedback.setText("User added successfully.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not add user",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
