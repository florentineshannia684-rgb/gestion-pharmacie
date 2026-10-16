package com.pharmacie.view.panels;

import com.pharmacie.model.Approvisionnement;
import com.pharmacie.model.Fournisseur;
import com.pharmacie.service.FournisseurService;
import com.pharmacie.util.AppException;
import com.pharmacie.util.FormatUtil;
import com.pharmacie.util.UIHelper;
import com.pharmacie.view.components.ModulePanel;
import com.pharmacie.view.components.UIFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Module de gestion des fournisseurs : liste, recherche, CRUD, approvisionnements.
 */
public class FournisseurPanel extends ModulePanel {

    private final FournisseurService service = new FournisseurService();

    private final JTextField champRecherche = UIFactory.champRecherche("Rechercher un fournisseur...");
    private final DefaultTableModel modele = UIFactory.modeleTable(
            "ID", "Nom", "Contact", "Telephone", "Email", "Adresse");
    private final JTable table = new JTable(modele);

    private List<Fournisseur> donnees;

    public FournisseurPanel() {
        super("Fournisseurs", "Gestion des fournisseurs et de leurs livraisons");

        champRecherche.addActionListener(e -> rafraichir());
        UIFactory.liveSearch(champRecherche, this::rafraichir);

        JButton btnNouveau = UIFactory.boutonPrincipal("+ Nouveau");
        btnNouveau.addActionListener(e -> ouvrirFormulaire(null));
        JButton btnModifier = UIFactory.boutonSecondaire("Modifier");
        btnModifier.addActionListener(e -> modifierSelection());
        JButton btnAppro = UIFactory.boutonSecondaire("Approvisionnements");
        btnAppro.addActionListener(e -> ouvrirApprovisionnements());
        JButton btnSupprimer = UIFactory.boutonDanger("Supprimer");
        btnSupprimer.addActionListener(e -> supprimerSelection());

        ajouterAction(champRecherche);
        ajouterAction(btnNouveau);
        ajouterAction(btnModifier);
        ajouterAction(btnAppro);
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
        donnees = service.rechercher(champRecherche.getText().trim());
        modele.setRowCount(0);
        for (Fournisseur f : donnees) {
            modele.addRow(new Object[]{
                    f.getId(), f.getNom(),
                    f.getContact() == null ? "-" : f.getContact(),
                    f.getTelephone() == null ? "-" : f.getTelephone(),
                    f.getEmail() == null ? "-" : f.getEmail(),
                    f.getAdresse() == null ? "-" : f.getAdresse()});
        }
        setSousTitre(donnees.size() + " fournisseur(s)");
    }

    private Fournisseur selection() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) modele.getValueAt(row, 0);
        return donnees.stream().filter(f -> f.getId() == id).findFirst().orElse(null);
    }

    private void modifierSelection() {
        Fournisseur f = selection();
        if (f == null) {
            UIHelper.avertissement(this, "Selectionnez un fournisseur a modifier.");
            return;
        }
        ouvrirFormulaire(f);
    }

    private void supprimerSelection() {
        Fournisseur f = selection();
        if (f == null) {
            UIHelper.avertissement(this, "Selectionnez un fournisseur a supprimer.");
            return;
        }
        if (UIHelper.confirmer(this, "Supprimer le fournisseur \"" + f.getNom() + "\" ?")) {
            try {
                service.supprimer(f.getId());
                UIHelper.succes(this, "Fournisseur supprime.");
                rafraichir();
            } catch (AppException ex) {
                UIHelper.erreur(this, ex.getMessage());
            }
        }
    }

    private void ouvrirApprovisionnements() {
        Fournisseur f = selection();
        if (f == null) {
            UIHelper.avertissement(this, "Selectionnez un fournisseur.");
            return;
        }
        new ApprovisionnementsDialog(f).setVisible(true);
    }

    private void ouvrirFormulaire(Fournisseur existant) {
        new FournisseurDialog(existant).setVisible(true);
    }

    // ------------------------------------------------------------ DIALOGUE APPRO

    private class ApprovisionnementsDialog extends JDialog {
        ApprovisionnementsDialog(Fournisseur fournisseur) {
            super((Frame) SwingUtilities.getWindowAncestor(FournisseurPanel.this),
                    "Approvisionnements - " + fournisseur.getNom(), true);

            DefaultTableModel m = UIFactory.modeleTable("Reference", "Date", "Montant");
            JTable t = new JTable(m);
            for (Approvisionnement a : service.approvisionnements(fournisseur.getId())) {
                m.addRow(new Object[]{
                        a.getReference(),
                        FormatUtil.dateHeure(a.getDateAppro()),
                        FormatUtil.montant(a.getMontantTotal())});
            }

            JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            boutons.setBackground(Color.WHITE);
            JButton fermer = UIFactory.boutonSecondaire("Fermer");
            fermer.addActionListener(e -> dispose());
            boutons.add(fermer);

            setLayout(new BorderLayout());
            add(UIFactory.tableStylisee(t), BorderLayout.CENTER);
            add(boutons, BorderLayout.SOUTH);
            setSize(560, 400);
            setLocationRelativeTo(FournisseurPanel.this);
        }
    }

    // ------------------------------------------------------------ DIALOGUE

    private class FournisseurDialog extends JDialog {
        private final JTextField nom = new JTextField(16);
        private final JTextField contact = new JTextField(16);
        private final JTextField telephone = new JTextField(16);
        private final JTextField email = new JTextField(16);
        private final JTextField adresse = new JTextField(16);
        private final Fournisseur fournisseur;

        FournisseurDialog(Fournisseur existant) {
            super((Frame) SwingUtilities.getWindowAncestor(FournisseurPanel.this),
                    existant == null ? "Nouveau fournisseur" : "Modifier le fournisseur", true);
            this.fournisseur = existant != null ? existant : new Fournisseur();

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(Color.WHITE);
            form.setBorder(BorderFactory.createEmptyBorder(18, 18, 8, 18));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(6, 6, 6, 6);
            g.anchor = GridBagConstraints.WEST;
            g.fill = GridBagConstraints.HORIZONTAL;

            int y = 0;
            y = ligne(form, g, y, "Nom *", nom);
            y = ligne(form, g, y, "Contact", contact);
            y = ligne(form, g, y, "Telephone", telephone);
            y = ligne(form, g, y, "Email", email);
            y = ligne(form, g, y, "Adresse", adresse);

            if (existant != null) {
                nom.setText(fournisseur.getNom());
                contact.setText(fournisseur.getContact());
                telephone.setText(fournisseur.getTelephone());
                email.setText(fournisseur.getEmail());
                adresse.setText(fournisseur.getAdresse());
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
            setLocationRelativeTo(FournisseurPanel.this);
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
                fournisseur.setNom(nom.getText().trim());
                fournisseur.setContact(contact.getText().trim());
                fournisseur.setTelephone(telephone.getText().trim());
                fournisseur.setEmail(email.getText().trim());
                fournisseur.setAdresse(adresse.getText().trim());

                service.enregistrer(fournisseur);
                UIHelper.succes(this, "Fournisseur enregistre.");
                dispose();
                rafraichir();
            } catch (AppException ex) {
                UIHelper.erreur(this, ex.getMessage());
            }
        }
    }
}
