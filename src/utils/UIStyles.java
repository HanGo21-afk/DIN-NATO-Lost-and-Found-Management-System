package utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;


public final class UIStyles {
    private UIStyles() {
    }

    
    public static final Color NAVY = Color.decode("#172B4D");
    public static final Color NAVY_HOVER = Color.decode("#233E6B");
    public static final Color WHITE = Color.WHITE;
    public static final Color LIGHT_GRAY = Color.decode("#F3F6FA");
    public static final Color PRIMARY = Color.decode("#3478F6");
    public static final Color PRIMARY_HOVER = Color.decode("#2563DB");
    public static final Color TEXT = Color.decode("#253247");
    public static final Color TEXT_MUTED = Color.decode("#6B778C");
    public static final Color BORDER = Color.decode("#DFE5ED");
    public static final Color SUCCESS = Color.decode("#2E9D68");
    public static final Color WARNING = Color.decode("#E6A23C");
    public static final Color ERROR = Color.decode("#D9534F");
    public static final Color SELECTION = Color.decode("#E3EDFF");

    
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 24);
    public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);

    
    public static Color statusColor(String status) {
        if (status == null) return TEXT_MUTED;
        switch (status) {
            case "Claimed":
            case "Returned":
            case "Approved":
                return SUCCESS;
            case "Pending Claim":
            case "Pending":
            case "Match Found":
                return WARNING;
            case "Rejected":
                return ERROR;
            default:
                return PRIMARY; 
        }
    }

    
    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_TITLE);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_HEADING);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel subtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_MUTED);
        return label;
    }

    public static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BOLD);
        label.setForeground(TEXT);
        return label;
    }

    
    public static RoundedButton primaryButton(String text) {
        return new RoundedButton(text, PRIMARY, PRIMARY_HOVER, WHITE, null);
    }

    public static RoundedButton secondaryButton(String text) {
        return new RoundedButton(text, WHITE, LIGHT_GRAY, TEXT, BORDER);
    }

    public static RoundedButton navButton(String text) {
        RoundedButton button = new RoundedButton(text, NAVY, NAVY_HOVER, WHITE, null);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        return button;
    }

    
    public static void styleTextField(JTextComponent field) {
        field.setFont(FONT_BODY);
        field.setForeground(TEXT);
        field.setBackground(WHITE);
        field.setCaretColor(TEXT);
        field.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(7, 10, 7, 10)));
    }

    public static void styleCombo(JComboBox<?> combo) {
        combo.setFont(FONT_BODY);
        combo.setForeground(TEXT);
        combo.setBackground(WHITE);
        combo.setPreferredSize(new Dimension(Math.max(combo.getPreferredSize().width, 150), 36));
    }

    
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setForeground(TEXT);
        table.setBackground(WHITE);
        table.setRowHeight(36);
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(SELECTION);
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        table.setDefaultRenderer(Object.class, new CellRenderer());
    }

    public static JScrollPane tableScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(BORDER));
        scroll.getViewport().setBackground(WHITE);
        return scroll;
    }

    
    public static class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            setHorizontalAlignment(SwingConstants.LEFT);
            return this;
        }
    }

    
    public static class StatusCellRenderer extends CellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setForeground(statusColor(String.valueOf(value)));
            setFont(FONT_BOLD);
            return this;
        }
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);
            setBackground(LIGHT_GRAY);
            setForeground(TEXT_MUTED);
            setFont(FONT_BOLD);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(0, 10, 0, 10)));
            return this;
        }
    }

    

    
    public static class RoundedButton extends JButton {
        private Color baseColor;
        private Color hoverColor;
        private final Color borderColor;
        private boolean hovering = false;

        public RoundedButton(String text, Color base, Color hover, Color textColor, Color border) {
            super(text);
            this.baseColor = base;
            this.hoverColor = hover;
            this.borderColor = border;
            setForeground(textColor);
            setFont(FONT_BOLD);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(9, 18, 9, 18));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovering = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovering = false;
                    repaint();
                }
            });
        }

        
        public void setColors(Color base, Color hover) {
            this.baseColor = base;
            this.hoverColor = hover;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hovering ? hoverColor : baseColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    
    public static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color borderColor;

        public RoundedPanel(int radius, Color background, Color border) {
            this.radius = radius;
            this.borderColor = border;
            setBackground(background);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
