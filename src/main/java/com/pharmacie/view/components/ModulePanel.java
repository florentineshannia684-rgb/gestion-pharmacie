package com.pharmacie.view.components;

import com.pharmacie.util.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Panneau de base pour tous les modules de l'application.
 * Fournit un en-tete (titre + sous-titre + zone d'actions) et une zone de contenu.
 */
public abstract class ModulePanel extends JPanel {

    private final JPanel zoneActions;
    private final JPanel contenu;
    private final JLabel sousTitreLabel;

    protected ModulePanel(String titre, String sousTitre) {
        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        // --- En-tete ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titreBloc = new JPanel();
        titreBloc.setOpaque(false);
        titreBloc.setLayout(new BoxLayout(titreBloc, BoxLayout.Y_AXIS));
        JLabel titreLabel = new JLabel(titre);
        titreLabel.setFont(UITheme.FONT_TITLE);
        titreLabel.setForeground(UITheme.TEXT);
        titreLabel.setAlignmentX(LEFT_ALIGNMENT);
        sousTitreLabel = new JLabel(sousTitre == null ? " " : sousTitre);
        sousTitreLabel.setFont(UITheme.FONT_SMALL);
        sousTitreLabel.setForeground(UITheme.TEXT_MUTED);
        sousTitreLabel.setAlignmentX(LEFT_ALIGNMENT);
        titreBloc.add(titreLabel);
        titreBloc.add(Box.createVerticalStrut(3));
        titreBloc.add(sousTitreLabel);

        zoneActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        zoneActions.setOpaque(false);

        header.add(titreBloc, BorderLayout.WEST);
        header.add(zoneActions, BorderLayout.EAST);

        contenu = new JPanel(new BorderLayout());
        contenu.setOpaque(false);

        add(header, BorderLayout.NORTH);
        add(contenu, BorderLayout.CENTER);
    }

    /** Ajoute un composant (bouton, champ) dans la zone d'actions de l'en-tete. */
    protected void ajouterAction(JComponent c) {
        zoneActions.add(c);
    }

    /** Definit le composant principal du module. */
    protected void setContenu(Component c) {
        contenu.removeAll();
        contenu.add(c, BorderLayout.CENTER);
        contenu.revalidate();
        contenu.repaint();
    }

    protected void setSousTitre(String s) {
        sousTitreLabel.setText(s);
    }

    /** Appelee par la fenetre principale quand le module devient visible. */
    public abstract void rafraichir();
}
