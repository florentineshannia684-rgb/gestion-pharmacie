package com.pharmacie.view.panels;

import com.pharmacie.model.Categorie;
import com.pharmacie.service.CategorieService;
import com.pharmacie.util.AppException;
import com.pharmacie.util.UIHelper;
import com.pharmacie.view.components.ModulePanel;
import com.pharmacie.view.components.UIFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Module de gestion des categories de medicaments : liste, CRUD.
 */
public class CategoriePanel extends ModulePanel {

    private final CategorieService service = new CategorieService();

    private final DefaultTableModel modele = UIFactory.modeleTable("ID", "Nom", "Description");
    private final JTable table = new JTable(modele);

    private List<Categorie> donnees;

    public CategoriePanel() {
        super("Categories", "Classification des medicaments");

        JButton btnNouveau = UIFactory.boutonPrincipal("+ Nouveau");
        btnNouveau.addActionListener(e -> ouvrirFormulaire(null));
        JButton btnModifier = UIFactory.boutonSecondaire("Modifier");
        btnModifier.addActionListener(e -> modifierSelection());
        JButton btnSupprimer = UIFactory.boutonDanger("Supprimer");
        btnSupprimer.addActionListener(e -> supprimerSelection());

        ajouterAction(btnNouveau);
        ajouterAction(btnModifier);
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
        for (Categorie c : donnees) {
            modele.addRow(new Object[]{
                    c.getId(), c.getNom(),
                    c.getDescription() == null ? "-" : c.getDescription()});
        }
        setSousTitre(donnees.size() + " categorie(s)");
    }

    private Categorie selection() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) modele.getValueAt(row, 0);
        return donnees.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

    private void modifierSelection() {
        Categorie c = selection();
        if (c == null) {
            UIHelper.avertissement(this, "Selectionnez une categorie a modifier.");
            return;
        }
        ouvrirFormulaire(c);
    }

    private void supprimerSelection() {
        Categorie c = selection();
        if (c == null) {
            UIHelper.avertissement(this, "Selectionnez une categorie a supprimer.");
            return;
        }
        if (UIHelper.confirmer(this, "Supprimer la categorie \"" + c.getNom() + "\" ?")) {
            try {
                service.supprimer(c.getId());
                UIHelper.succes(this, "Categorie supprimee.");
                rafraichir();
            } catch (AppException ex) {
                UIHelper.erreur(this, ex.getMessage());
            }
        }
    }

    private void ouvrirFormulaire(Categorie existant) {
        new CategorieDialog(existant).setVisible(true);
    }

    // ------------------------------------------------------------ DIALOGUE

    private class CategorieDialog extends JDialog {
        private final JTextField nom = new JTextField(16);
        private final JTextField description = new JTextField(16);
        private final Categorie categorie;

        CategorieDialog(Categorie existant) {
            super((Frame) SwingUtilities.getWindowAncestor(CategoriePanel.this),
                    existant == null ? "Nouvelle categorie" : "Modifier la categorie", true);
            this.categorie = existant != null ? existant : new Categorie();

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(Color.WHITE);
            form.setBorder(BorderFactory.createEmptyBorder(18, 18, 8, 18));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(6, 6, 6, 6);
            g.anchor = GridBagConstraints.WEST;
            g.fill = GridBagConstraints.HORIZONTAL;

            int y = 0;
            y = ligne(form, g, y, "Nom *", nom);
            y = ligne(form, g, y, "Description", description);

            if (existant != null) {
                nom.setText(categorie.getNom());
                description.setText(categorie.getDescription());
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
            setLocationRelativeTo(CategoriePanel.this);
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
                categorie.setNom(nom.getText().trim());
                categorie.setDescription(description.getText().trim());

                service.enregistrer(categorie);
                UIHelper.succes(this, "Categorie enregistree.");
                dispose();
                rafraichir();
            } catch (AppException ex) {
                UIHelper.erreur(this, ex.getMessage());
            }
        }
    }
}
