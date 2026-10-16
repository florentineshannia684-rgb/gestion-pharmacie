package com.pharmacie.view.panels;

import com.pharmacie.model.Role;
import com.pharmacie.model.Utilisateur;
import com.pharmacie.service.UtilisateurService;
import com.pharmacie.util.AppException;
import com.pharmacie.util.UIHelper;
import com.pharmacie.view.components.ModulePanel;
import com.pharmacie.view.components.UIFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Module de gestion des comptes utilisateurs et des roles (reserve a l'administrateur).
 */
public class UtilisateurPanel extends ModulePanel {

    private final UtilisateurService service = new UtilisateurService();

    private final DefaultTableModel modele = UIFactory.modeleTable(
            "ID", "Identifiant", "Nom complet", "Role", "Actif");
    private final JTable table = new JTable(modele);

    private List<Utilisateur> donnees;

    public UtilisateurPanel() {
        super("Utilisateurs", "Gestion des comptes et des roles (reserve a l'administrateur)");

        JButton btnNouveau = UIFactory.boutonPrincipal("+ Nouveau");
        btnNouveau.addActionListener(e -> ouvrirFormulaire(null));
        JButton btnModifier = UIFactory.boutonSecondaire("Modifier");
        btnModifier.addActionListener(e -> modifierSelection());
        JButton btnMotDePasse = UIFactory.boutonSecondaire("Changer mot de passe");
        btnMotDePasse.addActionListener(e -> changerMotDePasse());
        JButton btnSupprimer = UIFactory.boutonDanger("Supprimer");
        btnSupprimer.addActionListener(e -> supprimerSelection());

        ajouterAction(btnNouveau);
        ajouterAction(btnModifier);
        ajouterAction(btnMotDePasse);
        ajouterAction(btnSupprimer);

        table.getColumnModel().getColumn(0).setMaxWidth(44);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) modifierSelection();
            }
        });

        setContenu(UIFactory.tableStylisee(table));
    }

    @Override
    public void rafraichir() {
        donnees = service.lister();
        modele.setRowCount(0);
        for (Utilisateur u : donnees) {
            modele.addRow(new Object[]{
                    u.getId(), u.getNomUtilisateur(), u.getNomComplet(),
                    u.getRole() == null ? "-" : u.getRole().getLibelle(),
                    u.isActif() ? "Oui" : "Non"});
        }
        setSousTitre(donnees.size() + " utilisateur(s)");
    }

    private Utilisateur selection() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) modele.getValueAt(row, 0);
        return donnees.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
    }

    private void modifierSelection() {
        Utilisateur u = selection();
        if (u == null) {
            UIHelper.avertissement(this, "Selectionnez un utilisateur a modifier.");
            return;
        }
        ouvrirFormulaire(u);
    }

    private void supprimerSelection() {
        Utilisateur u = selection();
        if (u == null) {
            UIHelper.avertissement(this, "Selectionnez un utilisateur a supprimer.");
            return;
        }
        if (UIHelper.confirmer(this, "Supprimer l'utilisateur \"" + u.getNomUtilisateur() + "\" ?")) {
            try {
                service.supprimer(u.getId());
                UIHelper.succes(this, "Utilisateur supprime.");
                rafraichir();
            } catch (AppException ex) {
                UIHelper.erreur(this, ex.getMessage());
            }
        }
    }

    private void changerMotDePasse() {
        Utilisateur u = selection();
        if (u == null) {
            UIHelper.avertissement(this, "Selectionnez un utilisateur.");
            return;
        }
        new MotDePasseDialog(u).setVisible(true);
    }

    private void ouvrirFormulaire(Utilisateur existant) {
        new UtilisateurDialog(existant).setVisible(true);
    }

    // ------------------------------------------------------------ DIALOGUE MOT DE PASSE

    private class MotDePasseDialog extends JDialog {
        private final JPasswordField motDePasse = new JPasswordField(16);
        private final Utilisateur utilisateur;

        MotDePasseDialog(Utilisateur u) {
            super((Frame) SwingUtilities.getWindowAncestor(UtilisateurPanel.this),
                    "Changer le mot de passe - " + u.getNomUtilisateur(), true);
            this.utilisateur = u;

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(Color.WHITE);
            form.setBorder(BorderFactory.createEmptyBorder(18, 18, 8, 18));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(6, 6, 6, 6);
            g.anchor = GridBagConstraints.WEST;
            g.fill = GridBagConstraints.HORIZONTAL;

            UIFactory.showPasswordToggle(motDePasse);
            g.gridx = 0; g.gridy = 0; g.weightx = 0;
            form.add(UIFactory.labelChamp("Nouveau mot de passe *"), g);
            g.gridx = 1; g.weightx = 1;
            form.add(motDePasse, g);

            JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            boutons.setBackground(Color.WHITE);
            JButton annuler = UIFactory.boutonSecondaire("Annuler");
            annuler.addActionListener(e -> dispose());
            JButton enregistrer = UIFactory.boutonPrincipal("Enregistrer");
            enregistrer.addActionListener(e -> enregistrer());
            boutons.add(annuler);
            boutons.add(enregistrer);

            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(boutons, BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(UtilisateurPanel.this);
        }

        private void enregistrer() {
            try {
                service.changerMotDePasse(utilisateur.getId(), new String(motDePasse.getPassword()));
                UIHelper.succes(this, "Mot de passe modifie.");
                dispose();
            } catch (AppException ex) {
                UIHelper.erreur(this, ex.getMessage());
            }
        }
    }

    // ------------------------------------------------------------ DIALOGUE

    private class UtilisateurDialog extends JDialog {
        private final JTextField nomUtilisateur = new JTextField(16);
        private final JTextField nomComplet = new JTextField(16);
        private final JComboBox<Role> role = new JComboBox<>(Role.values());
        private final JCheckBox actif = new JCheckBox("Compte actif");
        private final JPasswordField motDePasse = new JPasswordField(16);
        private final Utilisateur utilisateur;
        private final boolean creation;

        UtilisateurDialog(Utilisateur existant) {
            super((Frame) SwingUtilities.getWindowAncestor(UtilisateurPanel.this),
                    existant == null ? "Nouvel utilisateur" : "Modifier l'utilisateur", true);
            this.creation = existant == null;
            this.utilisateur = existant != null ? existant : new Utilisateur();
            UIFactory.showPasswordToggle(motDePasse);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(Color.WHITE);
            form.setBorder(BorderFactory.createEmptyBorder(18, 18, 8, 18));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(6, 6, 6, 6);
            g.anchor = GridBagConstraints.WEST;
            g.fill = GridBagConstraints.HORIZONTAL;

            actif.setBackground(Color.WHITE);

            int y = 0;
            y = ligne(form, g, y, "Identifiant *", nomUtilisateur);
            y = ligne(form, g, y, "Nom complet *", nomComplet);
            y = ligne(form, g, y, "Role", role);
            y = ligne(form, g, y, "Actif", actif);
            if (creation) {
                y = ligne(form, g, y, "Mot de passe *", motDePasse);
            }

            if (creation) {
                actif.setSelected(true);
                role.setSelectedItem(Role.VENDEUR);
            } else {
                nomUtilisateur.setText(utilisateur.getNomUtilisateur());
                nomComplet.setText(utilisateur.getNomComplet());
                if (utilisateur.getRole() != null) role.setSelectedItem(utilisateur.getRole());
                actif.setSelected(utilisateur.isActif());
            }

            JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            boutons.setBackground(Color.WHITE);
            JButton annuler = UIFactory.boutonSecondaire("Annuler");
            annuler.addActionListener(e -> dispose());
            JButton enregistrer = UIFactory.boutonPrincipal("Enregistrer");
            enregistrer.addActionListener(e -> enregistrer());
            boutons.add(annuler);
            boutons.add(enregistrer);

            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(boutons, BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(UtilisateurPanel.this);
        }

        private int ligne(JPanel p, GridBagConstraints g, int y, String libelle, JComponent champ) {
            g.gridx = 0; g.gridy = y; g.weightx = 0;
            p.add(UIFactory.labelChamp(libelle), g);
            g.gridx = 1; g.weightx = 1;
            p.add(champ, g);
            return y + 1;
        }

        private void enregistrer() {
            try {
                utilisateur.setNomUtilisateur(nomUtilisateur.getText().trim());
                utilisateur.setNomComplet(nomComplet.getText().trim());
                utilisateur.setRole((Role) role.getSelectedItem());
                utilisateur.setActif(actif.isSelected());

                if (creation) {
                    service.creer(utilisateur, new String(motDePasse.getPassword()));
                } else {
                    service.modifier(utilisateur);
                }
                UIHelper.succes(this, "Utilisateur enregistre.");
                dispose();
                rafraichir();
            } catch (AppException ex) {
                UIHelper.erreur(this, ex.getMessage());
            }
        }
    }
}
