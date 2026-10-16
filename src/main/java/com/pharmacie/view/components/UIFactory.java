package com.pharmacie.view.components;

import com.pharmacie.util.UITheme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Fabrique de composants Swing stylises reutilisables (boutons, champs, tableaux...).
 */
public final class UIFactory {

    private UIFactory() {
    }

    // ------------------------------------------------------------------ BOUTONS

    public static JButton bouton(String texte, Color fond) {
        return bouton(texte, fond, Color.WHITE);
    }

    public static JButton bouton(String texte, Color fond, Color couleurTexte) {
        JButton b = new JButton(texte);
        b.setFont(UITheme.FONT_BOLD);
        b.setForeground(couleurTexte);
        b.setBackground(fond);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "roundRect");
        String ic = iconForLabel(texte);
        if (ic != null) {
            b.setIcon(IconFactory.icon(ic, 16, couleurTexte));
            b.setIconTextGap(8);
        }
        final Color base = fond;
        final Color hover = fond.darker();
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(base); }
        });
        return b;
    }

    public static JButton boutonPrincipal(String texte) {
        return bouton(texte, UITheme.PRIMARY);
    }

    public static JButton boutonSecondaire(String texte) {
        JButton b = bouton(texte, UITheme.CARD_BG, UITheme.TEXT);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(8, 17, 8, 17)));
        return b;
    }

    /** Associe une icone a un bouton selon les mots-cles de son libelle. */
    private static String iconForLabel(String texte) {
        String s = texte == null ? "" : texte.toLowerCase();
        if (s.contains("nouveau") || s.contains("nouvel") || s.startsWith("+") || s.contains("ajouter")) return "add";
        if (s.contains("modifier")) return "edit";
        if (s.contains("supprim")) return "delete";
        if (s.contains("imprim")) return "print";
        if (s.contains("actualiser") || s.contains("rafraich") || s.contains("generer") || s.contains("recharger")) return "refresh";
        if (s.contains("historique")) return "history";
        if (s.contains("approvisionnement")) return "appro";
        if (s.contains("recu") || s.contains("voir") || s.contains("detail") || s.contains("apercu") || s.contains("consulter")) return "view";
        if (s.contains("rechercher")) return "search";
        if (s.contains("deconnexion")) return "logout";
        if (s.contains("valider") || s.contains("enregistrer") || s.contains("connecter") || s.contains("connexion") || s.contains("changer")) return "check";
        if (s.contains("annuler") || s.contains("vider") || s.contains("retirer") || s.contains("fermer")) return "close";
        return null;
    }

    public static JButton boutonDanger(String texte) {
        return bouton(texte, UITheme.DANGER);
    }

    public static JButton boutonSucces(String texte) {
        return bouton(texte, UITheme.SUCCESS);
    }

    // ------------------------------------------------------------------ CHAMPS

    public static JTextField champTexte() {
        JTextField f = new JTextField();
        f.setFont(UITheme.FONT_NORMAL);
        f.putClientProperty("JComponent.roundRect", true);
        return f;
    }

    public static JTextField champRecherche(String placeholder) {
        JTextField f = champTexte();
        f.putClientProperty("JTextField.placeholderText", placeholder);
        f.putClientProperty("JTextField.leadingIcon", IconFactory.icon("search", 16, UITheme.TEXT_MUTED));
        f.putClientProperty("JTextField.showClearButton", true);
        f.setColumns(22);
        return f;
    }

    /**
     * Ajoute un bouton "oeil" dans le champ mot de passe pour afficher / masquer
     * la saisie (composant integre au champ, fonctionne avec FlatLaf).
     */
    public static void showPasswordToggle(JPasswordField champ) {
        final char echoDefaut = champ.getEchoChar() != 0 ? champ.getEchoChar() : '•';
        champ.setEchoChar(echoDefaut);
        JToggleButton oeil = new JToggleButton(IconFactory.icon("view", 16, UITheme.TEXT_MUTED));
        oeil.setContentAreaFilled(false);
        oeil.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 8));
        oeil.setFocusable(false);
        oeil.setCursor(new Cursor(Cursor.HAND_CURSOR));
        oeil.setToolTipText("Afficher / masquer le mot de passe");
        oeil.addActionListener(e -> {
            champ.setEchoChar(oeil.isSelected() ? (char) 0 : echoDefaut);
            oeil.setIcon(IconFactory.icon("view", 16,
                    oeil.isSelected() ? UITheme.PRIMARY : UITheme.TEXT_MUTED));
        });
        champ.putClientProperty("JTextField.trailingComponent", oeil);
    }

    /** Declenche {@code action} a chaque frappe (recherche instantanee). */
    public static void liveSearch(JTextField champ, Runnable action) {
        champ.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
        });
    }

    public static JLabel label(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(UITheme.FONT_NORMAL);
        l.setForeground(UITheme.TEXT);
        return l;
    }

    public static JLabel labelChamp(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT);
        return l;
    }

    public static JLabel titre(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(UITheme.FONT_TITLE);
        l.setForeground(UITheme.TEXT);
        return l;
    }

    // ------------------------------------------------------------------ TABLEAUX

    /** Modele de tableau non editable. */
    public static DefaultTableModel modeleTable(String... colonnes) {
        return new DefaultTableModel(colonnes, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    /** Applique un style moderne a un tableau (lignes alternees, en-tete colore). */
    public static JScrollPane tableStylisee(JTable table) {
        table.setFont(UITheme.FONT_NORMAL);
        table.setRowHeight(36);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setGridColor(new Color(0xEEEEEE));
        table.setSelectionBackground(UITheme.PRIMARY_LIGHT);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        // Lignes alternees + marge interne, applique a toutes les colonnes (modele = Object.class)
        table.setDefaultRenderer(Object.class, new StripedCellRenderer());

        JTableHeader header = table.getTableHeader();
        header.setFont(UITheme.FONT_BOLD);
        header.setBackground(UITheme.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    // ------------------------------------------------------------------ PANELS

    public static JPanel carteBlanche() {
        JPanel p = new JPanel();
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        return p;
    }

    public static Border paddingBorder(int t, int l, int b, int r) {
        return BorderFactory.createEmptyBorder(t, l, b, r);
    }
}
