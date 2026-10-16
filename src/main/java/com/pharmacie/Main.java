package com.pharmacie;

import com.formdev.flatlaf.FlatLightLaf;
import com.pharmacie.config.ConnectionManager;
import com.pharmacie.service.DatabaseBootstrap;
import com.pharmacie.util.UITheme;
import com.pharmacie.view.LoginFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Point d'entree de l'application de gestion de pharmacie.
 */
public class Main {

    public static void main(String[] args) {
        // Look & Feel moderne
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("TableHeader.height", 38);
        } catch (Exception e) {
            System.err.println("FlatLaf indisponible, look par defaut : " + e.getMessage());
        }

        // Verification de la connexion a la base
        if (!ConnectionManager.testConnection()) {
            JOptionPane.showMessageDialog(null,
                    "Impossible de se connecter a la base de donnees MySQL.\n\n" +
                    "Verifiez que :\n" +
                    "  1. MySQL (XAMPP) est demarre.\n" +
                    "  2. La base 'pharmacie' a ete creee (script src/main/resources/sql/schema.sql).\n" +
                    "  3. Les identifiants dans db.properties sont corrects.\n",
                    "Erreur de connexion", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Garantit l'existence des comptes par defaut (hash valides)
        try {
            new DatabaseBootstrap().initialiser();
        } catch (Exception e) {
            System.err.println("Avertissement bootstrap : " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
