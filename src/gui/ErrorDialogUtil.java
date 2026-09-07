package gui;

import java.awt.Component;

import javax.swing.JOptionPane;

public final class ErrorDialogUtil {

    private ErrorDialogUtil() {
        // Utility klasa
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Greska", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Informacija", JOptionPane.INFORMATION_MESSAGE);
    }
}