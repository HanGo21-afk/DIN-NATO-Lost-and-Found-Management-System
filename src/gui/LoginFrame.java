package gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import models.User;
import services.UserService;
import utils.UIStyles;


public class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel errorLabel = new JLabel(" ");

    public LoginFrame() {
        setTitle("DIN NATO - Sign In");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(840, 500);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(buildBrandPanel());
        root.add(buildFormPanel());
        setContentPane(root);
    }

    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIStyles.NAVY);

        JLabel name = new JLabel("DIN NATO");
        name.setFont(UIStyles.FONT_TITLE.deriveFont(38f));
        name.setForeground(UIStyles.WHITE);

        JLabel tagline = new JLabel("Loss and Found Management System");
        tagline.setFont(UIStyles.FONT_BODY.deriveFont(15f));
        tagline.setForeground(UIStyles.BORDER);

        JLabel line = new JLabel("Report it. Find it. Return it.");
        line.setFont(UIStyles.FONT_BODY);
        line.setForeground(UIStyles.TEXT_MUTED);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(name);
        box.add(javax.swing.Box.createVerticalStrut(8));
        box.add(tagline);
        box.add(javax.swing.Box.createVerticalStrut(24));
        box.add(line);
        panel.add(box);
        return panel;
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIStyles.WHITE);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        box.setPreferredSize(new Dimension(300, 360));
        box.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel title = UIStyles.title("Welcome back");
        JLabel sub = UIStyles.subtitle("Sign in to continue");

        UIStyles.styleTextField(usernameField);
        UIStyles.styleTextField(passwordField);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        errorLabel.setFont(UIStyles.FONT_SMALL);
        errorLabel.setForeground(UIStyles.ERROR);

        UIStyles.RoundedButton loginButton = UIStyles.primaryButton("Sign In");
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginButton.addActionListener(e -> login());
        passwordField.addActionListener(e -> login());
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());

        JLabel userLabel = UIStyles.fieldLabel("Username");
        JLabel passLabel = UIStyles.fieldLabel("Password");

        Component[] parts = {title, sub, userLabel, usernameField, passLabel, passwordField,
            errorLabel, loginButton};
        for (Component c : parts) {
            ((javax.swing.JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        box.add(title);
        box.add(javax.swing.Box.createVerticalStrut(4));
        box.add(sub);
        box.add(javax.swing.Box.createVerticalStrut(24));
        box.add(userLabel);
        box.add(javax.swing.Box.createVerticalStrut(6));
        box.add(usernameField);
        box.add(javax.swing.Box.createVerticalStrut(14));
        box.add(passLabel);
        box.add(javax.swing.Box.createVerticalStrut(6));
        box.add(passwordField);
        box.add(javax.swing.Box.createVerticalStrut(8));
        box.add(errorLabel);
        box.add(javax.swing.Box.createVerticalStrut(8));
        box.add(loginButton);

        panel.add(box);
        getRootPane().setDefaultButton(loginButton);
        return panel;
    }

    private void login() {
        User user;
        try {
            user = UserService.getInstance().authenticate(
                    usernameField.getText(), new String(passwordField.getPassword()));
        } catch (IllegalStateException ex) {
            errorLabel.setText("Cannot reach the database. Is MySQL running?");
            return;
        }
        if (user == null) {
            errorLabel.setText("Invalid username or password.");
            passwordField.setText("");
            return;
        }
        DashboardFrame dashboard = new DashboardFrame(user);
        dashboard.setVisible(true);
        dispose();
    }
}