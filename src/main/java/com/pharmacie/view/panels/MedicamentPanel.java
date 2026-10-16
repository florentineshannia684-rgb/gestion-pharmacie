package com.pharmacie.view.panels;

import com.pharmacie.model.Categorie;
import com.pharmacie.model.Medicament;
import com.pharmacie.service.CategorieService;
import com.pharmacie.service.MedicamentService;
import com.pharmacie.util.AppException;
import com.pharmacie.util.FormatUtil;
import com.pharmacie.util.UIHelper;
import com.pharmacie.util.UITheme;
import com.pharmacie.view.components.BadgeRenderer;
import com.pharmacie.view.components.ModulePanel;
import com.pharmacie.view.components.UIFactory;
import com.pharmacie.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Module de gestion des medicaments : liste, recherche, filtre, CRUD.
 */
public class MedicamentPanel extends ModulePanel {

    private final MedicamentService service = new MedicamentService();
    private final CategorieService categorieService = new CategorieService();

    private final JTextField champRecherche = UIFactory.champRecherche("Rechercher par nom ou reference...");
    private final JComboBox<Categorie> filtreCategorie = new JComboBox<>();
    private final DefaultTableModel modele = UIFactory.modeleTable(
            "ID", "Reference", "Nom", "Forme", "Dosage", "Prix", "Stock", "Seuil", "Categorie");
    private final JTable table = new JTable(modele);

    private List<Medicament> donnees;

    public MedicamentPanel() {
        super("Medicaments", "Gestion du catalogue de medicaments");

        champRecherche.addActionListener(e -> rafraichir());
        UIFactory.liveSearch(champRecherche, this::rafraichir);
        filtreCategorie.addActionListener(e -> rafraichir());

        JButton btnNouveau = UIFactory.boutonPrincipal("+ Nouveau");
        btnNouveau.addActionListener(e -> ouvrirFormulaire(null));
        JButton btnModifier = UIFactory.boutonSecondaire("Modifier");
        btnModifier.addActionListener(e -> modifierSelection());
        JButton btnSupprimer = UIFactory.boutonDanger("Supprimer");
        btnSupprimer.addActionListener(e -> supprimerSelection());

        ajouterAction(champRecherche);
        ajouterAction(filtreCategorie);
        ajouterAction(btnNouveau);
        ajouterAction(btnModifier);
        ajouterAction(btnSupprimer);

        table.getColumnModel().getColumn(0).setMaxWidth(44);
        // Pastille rouge sur la colonne Stock quand le stock est faible (suffixe " !")
        table.getColumnModel().getColumn(6).setCellRenderer(
                new BadgeRenderer(v -> v != null && v.contains("!") ? UITheme.DANGER : null));
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) modifierSelection();
            }
        });

        setContenu(UIFactory.tableStylisee(table));
    }

    @Override
    public void rafraichir() {
        chargerFiltreCategories();
        Integer catId = null;
        Object sel = filtreCategorie.getSelectedItem();
        if (sel instanceof Categorie c && c.getId() != 0) catId = c.getId();

        donnees = service.rechercher(champRecherche.getText().trim(), catId);
        modele.setRowCount(0);
        for (Medicament m : donnees) {
            modele.addRow(new Object[]{
                    m.getId(), m.getReference(), m.getNom(), m.getForme(), m.getDosage(),
                    FormatUtil.montant(m.getPrixVente()),
                    m.getQuantiteStock() + (m.isStockFaible() ? " !" : ""),
                    m.getSeuilStock(),
                    m.getCategorieNom() == null ? "-" : m.getCategorieNom()});
        }
        setSousTitre(donnees.size() + " medicament(s)");
    }

    private boolean chargementFiltre = false;

    private void chargerFiltreCategories() {
        chargementFiltre = true;
        Object prec = filtreCategorie.getSelectedItem();
        filtreCategorie.removeAllItems();
        Categorie toutes = new Categorie(0, "Toutes les categories", null);
        filtreCategorie.addItem(toutes);
        for (Categorie c : categorieService.lister()) filtreCategorie.addItem(c);
        if (prec instanceof Categorie pc) {
            for (int i = 0; i < filtreCategorie.getItemCount(); i++) {
                if (filtreCategorie.getItemAt(i).getId() == pc.getId()) {
                    filtreCategorie.setSelectedIndex(i);
                    break;
                }
            }
        }
        chargementFiltre = false;
    }

    private Medicament selection() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) modele.getValueAt(row, 0);
        return donnees.stream().filter(m -> m.getId() == id).findFirst().orElse(null);
    }

    private void modifierSelection() {
        Medicament m = selection();
        if (m == null) {
            UIHelper.avertissement(this, "Selectionnez un medicament a modifier.");
            return;
        }
        ouvrirFormulaire(m);
    }

    private void supprimerSelection() {
        Medicament m = selection();
        if (m == null) {
            UIHelper.avertissement(this, "Selectionnez un medicament a supprimer.");
            return;
        }
        if (UIHelper.confirmer(this, "Supprimer le medicament \"" + m.getNom() + "\" ?")) {
            try {
                service.supprimer(m.getId());
                UIHelper.succes(this, "Medicament supprime.");
                rafraichir();
            } catch (AppException ex) {
                UIHelper.erreur(this, ex.getMessage());
            }
        }
    }

    private void ouvrirFormulaire(Medicament existant) {
        new MedicamentDialog(existant).setVisible(true);
    }

    // ------------------------------------------------------------ DIALOGUE

    private class MedicamentDialog extends JDialog {
        private final JTextField ref = new JTextField(16);
        private final JTextField nom = new JTextField(16);
        private final JTextField forme = new JTextField(16);
        private final JTextField dosage = new JTextField(16);
        private final JTextField prix = new JTextField(16);
        private final JTextField seuil = new JTextField(16);
        private final JComboBox<Categorie> categorie = new JComboBox<>();
        private final Medicament medicament;

        MedicamentDialog(Medicament existant) {
            super((Frame) SwingUtilities.getWindowAncestor(MedicamentPanel.this),
                    existant == null ? "Nouveau medicament" : "Modifier le medicament", true);
            this.medicament = existant != null ? existant : new Medicament();

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(Color.WHITE);
            form.setBorder(BorderFactory.createEmptyBorder(18, 18, 8, 18));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(6, 6, 6, 6);
            g.anchor = GridBagConstraints.WEST;
            g.fill = GridBagConstraints.HORIZONTAL;

            categorie.addItem(new Categorie(0, "(Aucune)", null));
            for (Categorie c : categorieService.lister()) categorie.addItem(c);

            int y = 0;
            y = ligne(form, g, y, "Reference *", ref);
            y = ligne(form, g, y, "Nom *", nom);
            y = ligne(form, g, y, "Forme", forme);
            y = ligne(form, g, y, "Dosage", dosage);
            y = ligne(form, g, y, "Prix de vente (Ar) *", prix);
            y = ligne(form, g, y, "Seuil de stock", seuil);
            y = ligne(form, g, y, "Categorie", categorie);

            // Pre-remplissage
            if (existant == null) {
                ref.setText(service.genererReference());
                seuil.setText("10");
                prix.setText("0");
            } else {
                ref.setText(medicament.getReference());
                nom.setText(medicament.getNom());
                forme.setText(medicament.getForme());
                dosage.setText(medicament.getDosage());
                prix.setText(medicament.getPrixVente() == null ? "0" : medicament.getPrixVente().toPlainString());
                seuil.setText(String.valueOf(medicament.getSeuilStock()));
                if (medicament.getCategorieId() != null) {
                    for (int i = 0; i < categorie.getItemCount(); i++) {
                        if (categorie.getItemAt(i).getId() == medicament.getCategorieId()) {
                            categorie.setSelectedIndex(i);
                            break;
                        }
                    }
                }
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
            setLocationRelativeTo(MedicamentPanel.this);
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
                medicament.setReference(ref.getText().trim());
                medicament.setNom(nom.getText().trim());
                medicament.setForme(forme.getText().trim());
                medicament.setDosage(dosage.getText().trim());
                medicament.setPrixVente(parseMontant(prix.getText()));
                medicament.setSeuilStock(parseEntier(seuil.getText()));
                Categorie c = (Categorie) categorie.getSelectedItem();
                medicament.setCategorieId(c == null || c.getId() == 0 ? null : c.getId());

                service.enregistrer(medicament);
                UIHelper.succes(this, "Medicament enregistre.");
                dispose();
                rafraichir();
            } catch (AppException ex) {
                UIHelper.erreur(this, ex.getMessage());
            } catch (NumberFormatException ex) {
                UIHelper.erreur(this, "Prix ou seuil invalide.");
            }
        }

        private BigDecimal parseMontant(String s) {
            if (s == null || s.isBlank()) return BigDecimal.ZERO;
            return new BigDecimal(s.trim().replace(",", "."));
        }

        private int parseEntier(String s) {
            if (s == null || s.isBlank()) return 0;
            return Integer.parseInt(s.trim());
        }
    }
}
