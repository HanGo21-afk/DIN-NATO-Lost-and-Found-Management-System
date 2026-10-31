package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import models.Claim;
import models.Item;
import models.User;
import services.ClaimService;
import services.ItemService;
import utils.UIStyles;


public class ClaimsPanel extends JPanel {
    private final ClaimService claimService = ClaimService.getInstance();
    private final ItemService itemService = ItemService.getInstance();
    private final User user;
    private final Runnable onChange;

    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel feedback = new JLabel(" ");
    private List<Claim> shown = new ArrayList<>();

    public ClaimsPanel(User user, Runnable onChange) {
        this.user = user;
        this.onChange = onChange;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIStyles.WHITE);
        setBorder(new EmptyBorder(28, 32, 24, 32));

        JPanel titles = new JPanel(new BorderLayout(0, 4));
        titles.setOpaque(false);
        titles.add(UIStyles.title("Claims"), BorderLayout.NORTH);
        titles.add(UIStyles.subtitle("Owners can claim found items. Review each claim below."), BorderLayout.CENTER);

        UIStyles.RoundedButton newClaim = UIStyles.primaryButton("+ New Claim");
        newClaim.addActionListener(e -> onNewClaim());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(titles, BorderLayout.WEST);
        header.add(newClaim, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new String[]{"ID", "Item", "Claimant", "Date", "Status", "Remarks"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        UIStyles.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(4).setCellRenderer(new UIStyles.StatusCellRenderer());
        int[] widths = {50, 200, 160, 100, 100, 260};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        add(UIStyles.tableScroll(table), BorderLayout.CENTER);

        UIStyles.RoundedButton approve = UIStyles.secondaryButton("Approve");
        UIStyles.RoundedButton reject = UIStyles.secondaryButton("Reject");
        approve.addActionListener(e -> decide(true));
        reject.addActionListener(e -> decide(false));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.add(approve);
        actions.add(reject);

        feedback.setFont(UIStyles.FONT_BOLD);
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(actions, BorderLayout.WEST);
        south.add(feedback, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        shown = claimService.getAllClaims();
        model.setRowCount(0);
        for (Claim claim : shown) {
            Item item = itemService.getById(claim.getItemId());
            model.addRow(new Object[]{
                claim.getId(), item == null ? "(deleted)" : item.getName(), claim.getClaimantName(),
                claim.getClaimDate(), claim.getStatus(), claim.getRemarks()
            });
        }
    }

    private void showFeedback(String message, boolean ok) {
        feedback.setForeground(ok ? UIStyles.SUCCESS : UIStyles.ERROR);
        feedback.setText(message);
    }

    private void onNewClaim() {
        List<Item> claimable = claimService.getClaimableItems();
        if (claimable.isEmpty()) {
            JOptionPane.showMessageDialog(this, "There are no unclaimed found items right now.",
                    "Nothing to claim", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JComboBox<Item> itemBox = new JComboBox<>(claimable.toArray(new Item[0]));
        JTextField claimantField = new JTextField(user.getFullName(), 22);
        JTextArea remarksArea = new JTextArea(3, 22);
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        UIStyles.styleCombo(itemBox);
        UIStyles.styleTextField(claimantField);
        UIStyles.styleTextField(remarksArea);
        JScrollPane remarksScroll = new JScrollPane(remarksArea);
        remarksScroll.setBorder(new LineBorder(UIStyles.BORDER, 1, true));

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
        form.add(UIStyles.fieldLabel("Found item"));
        form.add(itemBox);
        form.add(UIStyles.fieldLabel("Claimant name"));
        form.add(claimantField);
        form.add(UIStyles.fieldLabel("Remarks (how the owner can prove it is theirs)"));

        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.add(form, BorderLayout.NORTH);
        wrapper.add(remarksScroll, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, wrapper, "New Claim",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            Item chosen = (Item) itemBox.getSelectedItem();
            claimService.createClaim(chosen.getId(), claimantField.getText(), remarksArea.getText());
            refresh();
            onChange.run();
            showFeedback("Claim submitted. The item is now marked Pending Claim.", true);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not submit claim",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void decide(boolean approve) {
        int row = table.getSelectedRow();
        if (row < 0 || row >= shown.size()) {
            JOptionPane.showMessageDialog(this, "Please select a claim first.",
                    "No selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Claim claim = shown.get(row);

        // A claim that was already decided cannot be decided again.
        if (!"Pending".equals(claim.getStatus())) {
            JOptionPane.showMessageDialog(this,
                    "Claim #" + claim.getId() + " was already " + claim.getStatus().toLowerCase()
                    + ". Only pending claims can be decided.",
                    "Already decided", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Ask before doing something that cannot be undone.
        Item item = itemService.getById(claim.getItemId());
        String itemName = item == null ? "(deleted item)" : item.getName();
        String action = approve ? "approve" : "reject";
        int answer = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + action + " claim #" + claim.getId()
                + " for \"" + itemName + "\"?\nThis cannot be undone.",
                approve ? "Approve claim" : "Reject claim",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;

        try {
            if (approve) {
                claimService.approve(claim.getId());
            } else {
                claimService.reject(claim.getId());
            }
            refresh();
            onChange.run();
            showFeedback(approve ? "Claim approved. The item is now Claimed." : "Claim rejected.", true);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not update claim",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}