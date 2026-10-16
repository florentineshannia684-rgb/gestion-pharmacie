package com.pharmacie.view.components;

import com.pharmacie.util.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Carte statistique du tableau de bord : un titre, une grande valeur,
 * une bande de couleur a gauche.
 */
public class StatCard extends JPanel {

    private final JLabel valeurLabel;
    private final JLabel titreLabel;
    private final JLabel sousTitreLabel;

    public StatCard(String titre, String valeur, String sousTitre, Color couleur) {
        setLayout(new BorderLayout(12, 0));
        setBackground(UITheme.CARD_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(14, 0, 14, 16)));

        // Fin accent de couleur a gauche
        JPanel bande = new JPanel();
        bande.setBackground(couleur);
        bande.setPreferredSize(new Dimension(4, 10));
        add(bande, BorderLayout.WEST);

        JPanel centre = new JPanel();
        centre.setOpaque(false);
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        titreLabel = new JLabel(titre.toUpperCase());
        titreLabel.setFont(UITheme.FONT_SMALL);
        titreLabel.setForeground(UITheme.TEXT_MUTED);
        titreLabel.setAlignmentX(LEFT_ALIGNMENT);

        valeurLabel = new JLabel(valeur);
        valeurLabel.setFont(UITheme.FONT_STAT);
        valeurLabel.setForeground(couleur);
        valeurLabel.setAlignmentX(LEFT_ALIGNMENT);

        sousTitreLabel = new JLabel(sousTitre == null ? " " : sousTitre);
        sousTitreLabel.setFont(UITheme.FONT_SMALL);
        sousTitreLabel.setForeground(UITheme.TEXT_MUTED);
        sousTitreLabel.setAlignmentX(LEFT_ALIGNMENT);

        centre.add(titreLabel);
        centre.add(Box.createVerticalStrut(6));
        centre.add(valeurLabel);
        centre.add(Box.createVerticalStrut(4));
        centre.add(sousTitreLabel);

        add(centre, BorderLayout.CENTER);
    }

    public void setValeur(String valeur) {
        valeurLabel.setText(valeur);
    }

    public void setSousTitre(String sousTitre) {
        sousTitreLabel.setText(sousTitre);
    }
}
