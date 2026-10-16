package com.pharmacie.util;

import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * Aides pour les boites de dialogue (messages, confirmations).
 */
public final class UIHelper {

    private UIHelper() {
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void succes(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Succes",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void erreur(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Erreur",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void avertissement(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Attention",
                JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirmer(Component parent, String message) {
        int r = JOptionPane.showConfirmDialog(parent, message, "Confirmation",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return r == JOptionPane.YES_OPTION;
    }
}
