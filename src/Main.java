import database.DatabaseConnection;
import gui.LoginFrame;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import utils.UIStyles;


public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {

        }
        UIManager.put("OptionPane.messageFont", UIStyles.FONT_BODY);
        UIManager.put("OptionPane.buttonFont", UIStyles.FONT_BODY);

        // Any error that nothing else catches ends up here and is shown to the user.
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            error.printStackTrace();
            showError(error);
        });

        SwingUtilities.invokeLater(() -> {
            if (databaseIsReachable()) {
                new LoginFrame().setVisible(true);
            } else {
                System.exit(1);
            }
        });
    }

    /** Tries to open one connection before the app starts. */
    private static boolean databaseIsReachable() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Cannot connect to the database.\n\n" + e.getMessage()
                    + "\n\nCheck that MySQL is running and that db.properties has the right"
                    + " URL, user, and password.",
                    "Database error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /** Shows a friendly message for an unexpected error. */
    private static void showError(Throwable error) {
        SwingUtilities.invokeLater(() -> {
            String message;
            if (error instanceof IllegalStateException) {
                message = "A database problem occurred.\n\n" + error.getMessage()
                        + "\n\nCheck that MySQL is still running, then try again.";
            } else {
                message = "Something went wrong.\n\n" + error.getMessage();
            }
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
}