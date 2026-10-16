package com.pharmacie.view.panels;

import com.pharmacie.model.Approvisionnement;
import com.pharmacie.model.Fournisseur;
import com.pharmacie.model.LigneApprovisionnement;
import com.pharmacie.model.Medicament;
import com.pharmacie.service.ApprovisionnementService;
import com.pharmacie.service.FournisseurService;
import com.pharmacie.service.MedicamentService;
import com.pharmacie.util.AppException;
import com.pharmacie.util.FormatUtil;
import com.pharmacie.util.UIHelper;
import com.pharmacie.view.components.ModulePanel;
import com.pharmacie.view.components.UIFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Module des approvisionnements : historique des receptions et saisie d'une
 * nouvelle livraison (qui augmente le stock via le service transactionnel).
 */
public class ApprovisionnementPanel extends ModulePanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ApprovisionnementService service = new ApprovisionnementService();
    private final FournisseurService fournisseurService = new FournisseurService();
    private final MedicamentService medicamentService = new MedicamentService();

    private final DefaultTableModel modele = UIFactory.modeleTable(
            "ID", "Reference", "Date", "Fournisseur", "Montant");
    private final JTable table = new JTable(modele);

    private List<Approvisionnement> donnees;

    public ApprovisionnementPanel() {
        super("Approvisionnements", "Reception des livraisons (augmente le stock)");

        JButton btnNouveau = UIFactory.boutonPrincipal("+ Nouvel approvisionnement");
        btnNouveau.addActionListener(e -> new ApproDialog().setVisible(true));
        JButton btnDetails = UIFactory.boutonSecondaire("Voir details");
        btnDetails.addActionListener(e -> voirDetails());

        ajouterAction(btnNouveau);
        ajouterAction(btnDetails);

        table.getColumnModel().getColumn(0).setMaxWidth(44);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) voirDetails();
            }
        });

        setContenu(UIFactory.tableStylisee(table));
    }

    @Override
    public void rafraichir() {
        donnees = service.lister();
        modele.setRowCount(0);
        for (Approvisionnement a : donnees) {
            modele.addRow(new Object[]{
                    a.getId(),
                    a.getReference(),
                    FormatUtil.dateHeure(a.getDateAppro()),
                    a.getFournisseurNom() == null ? "-" : a.getFournisseurNom(),
                    FormatUtil.montant(a.getMontantTotal())});
        }
        setSousTitre(donnees.size() + " approvisionnement(s)");
    }

    private Approvisionnement selection() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) modele.getValueAt(row, 0);
        return donnees.stream().filter(a -> a.getId() == id).findFirst().orElse(null);
    }

    private void voirDetails() {
        Approvisionnement a = selection();
        if (a == null) {
            UIHelper.avertissement(this, "Selectionnez un approvisionnement a consulter.");
            return;
        }
        new DetailDialog(a).setVisible(true);
    }

    // ------------------------------------------------------------ DETAILS

    private class DetailDialog extends JDialog {

        DetailDialog(Approvisionnement appro) {
            super((Frame) SwingUtilities.getWindowAncestor(ApprovisionnementPanel.this),
                    "Details - " + appro.getReference(), true);

            DefaultTableModel m = UIFactory.modeleTable(
                    "Medicament", "Qte", "Prix achat", "N Lot", "Expiration", "Sous-total");
            JTable t = new JTable(m);
            for (LigneApprovisionnement l : service.lignes(appro.getId())) {
                m.addRow(new Object[]{
                        l.getMedicamentNom(),
                        l.getQuantite(),
                        FormatUtil.montant(l.getPrixAchat()),
                        l.getNumeroLot(),
                        FormatUtil.date(l.getDateExpiration()),
                        FormatUtil.montant(l.getSousTotal())});
            }

            JButton fermer = UIFactory.boutonSecondaire("Fermer");
            fermer.addActionListener(e -> dispose());
            JPanel sud = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            sud.setBackground(Color.WHITE);
            sud.add(fermer);

            setLayout(new BorderLayout());
            add(UIFactory.tableStylisee(t), BorderLayout.CENTER);
            add(sud, BorderLayout.SOUTH);
            setSize(640, 420);
            setLocationRelativeTo(ApprovisionnementPanel.this);
        }
    }

    // ------------------------------------------------------------ SAISIE

    private class ApproDialog extends JDialog {

        private final JComboBox<Fournisseur> comboFournisseur = new JComboBox<>();
        private final JComboBox<Medicament> comboMedicament = new JComboBox<>();
        private final JTextField champQuantite = new JTextField(6);
        private final JTextField champPrix = new JTextField(8);
        private final JTextField champLot = new JTextField(10);
        private final JTextField champExpiration = new JTextField(10);

        private final DefaultTableModel modeleLignes = UIFactory.modeleTable(
                "Medicament", "Qte", "Prix achat", "N Lot", "Expiration", "Sous-total");
        private final JTable tableLignes = new JTable(modeleLignes);
        private final java.util.List<LigneApprovisionnement> lignes = new java.util.ArrayList<>();
        private final JLabel labelTotal = UIFactory.labelChamp("Total : " + FormatUtil.montant(BigDecimal.ZERO));

        ApproDialog() {
            super((Frame) SwingUtilities.getWindowAncestor(ApprovisionnementPanel.this),
                    "Nouvel approvisionnement", true);

            for (Fournisseur f : fournisseurService.lister()) comboFournisseur.addItem(f);
            for (Medicament m : medicamentService.lister()) comboMedicament.addItem(m);

            setLayout(new BorderLayout(0, 10));
            ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
            getContentPane().setBackground(Color.WHITE);

            add(construireHaut(), BorderLayout.NORTH);
            add(UIFactory.tableStylisee(tableLignes), BorderLayout.CENTER);
            add(construireBas(), BorderLayout.SOUTH);

            setSize(700, 560);
            setLocationRelativeTo(ApprovisionnementPanel.this);
        }

        private JPanel construireHaut() {
            JPanel haut = new JPanel();
            haut.setBackground(Color.WHITE);
            haut.setLayout(new BoxLayout(haut, BoxLayout.Y_AXIS));

            JPanel ligneFourn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            ligneFourn.setBackground(Color.WHITE);
            ligneFourn.add(UIFactory.labelChamp("Fournisseur"));
            comboFournisseur.setPreferredSize(new Dimension(260, 28));
            ligneFourn.add(comboFournisseur);

            JPanel ajout = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            ajout.setBackground(Color.WHITE);
            comboMedicament.setPreferredSize(new Dimension(220, 28));
            ajout.add(UIFactory.labelChamp("Medicament"));
            ajout.add(comboMedicament);
            ajout.add(UIFactory.labelChamp("Qte"));
            ajout.add(champQuantite);
            ajout.add(UIFactory.labelChamp("Prix achat"));
            ajout.add(champPrix);
            ajout.add(UIFactory.labelChamp("N Lot"));
            ajout.add(champLot);
            ajout.add(UIFactory.labelChamp("Expiration (jj/MM/aaaa)"));
            ajout.add(champExpiration);

            JButton btnAjouter = UIFactory.boutonSucces("Ajouter la ligne");
            btnAjouter.addActionListener(e -> ajouterLigne());
            JButton btnRetirer = UIFactory.boutonDanger("Retirer ligne");
            btnRetirer.addActionListener(e -> retirerLigne());
            JPanel boutonsLigne = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            boutonsLigne.setBackground(Color.WHITE);
            boutonsLigne.add(btnAjouter);
            boutonsLigne.add(btnRetirer);

            haut.add(ligneFourn);
            haut.add(ajout);
            haut.add(boutonsLigne);
            return haut;
        }

        private JPanel construireBas() {
            JPanel bas = new JPanel(new BorderLayout());
            bas.setBackground(Color.WHITE);

            JPanel gauche = new JPanel(new FlowLayout(FlowLayout.LEFT));
            gauche.setBackground(Color.WHITE);
            gauche.add(labelTotal);

            JPanel droite = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            droite.setBackground(Color.WHITE);
            JButton annuler = UIFactory.boutonSecondaire("Annuler");
            annuler.addActionListener(e -> dispose());
            JButton valider = UIFactory.boutonPrincipal("Valider la reception");
            valider.addActionListener(e -> valider());
            droite.add(annuler);
            droite.add(valider);

            bas.add(gauche, BorderLayout.WEST);
            bas.add(droite, BorderLayout.EAST);
            return bas;
        }

        private void ajouterLigne() {
            Medicament med = (Medicament) comboMedicament.getSelectedItem();
            if (med == null) {
                UIHelper.avertissement(this, "Selectionnez un medicament.");
                return;
            }
            int quantite;
            BigDecimal prix;
            try {
                quantite = Integer.parseInt(champQuantite.getText().trim());
            } catch (NumberFormatException ex) {
                UIHelper.erreur(this, "Quantite invalide.");
                return;
            }
            if (quantite <= 0) {
                UIHelper.erreur(this, "La quantite doit etre superieure a zero.");
                return;
            }
            try {
                String p = champPrix.getText().trim().replace(",", ".");
                prix = p.isEmpty() ? BigDecimal.ZERO : new BigDecimal(p);
            } catch (NumberFormatException ex) {
                UIHelper.erreur(this, "Prix d'achat invalide.");
                return;
            }
            LocalDate expiration;
            try {
                expiration = parseDate(champExpiration.getText());
            } catch (RuntimeException ex) {
                UIHelper.erreur(this, "Date invalide (jj/MM/aaaa)");
                return;
            }

            LigneApprovisionnement ligne = new LigneApprovisionnement();
            ligne.setMedicamentId(med.getId());
            ligne.setMedicamentNom(med.getNom());
            ligne.setQuantite(quantite);
            ligne.setPrixAchat(prix);
            ligne.setNumeroLot(champLot.getText().trim());
            ligne.setDateExpiration(expiration);
            lignes.add(ligne);

            modeleLignes.addRow(new Object[]{
                    ligne.getMedicamentNom(),
                    ligne.getQuantite(),
                    FormatUtil.montant(ligne.getPrixAchat()),
                    ligne.getNumeroLot(),
                    FormatUtil.date(ligne.getDateExpiration()),
                    FormatUtil.montant(ligne.getSousTotal())});

            champQuantite.setText("");
            champPrix.setText("");
            champLot.setText("");
            champExpiration.setText("");
            majTotal();
        }

        private void retirerLigne() {
            int row = tableLignes.getSelectedRow();
            if (row < 0) {
                UIHelper.avertissement(this, "Selectionnez une ligne a retirer.");
                return;
            }
            lignes.remove(row);
            modeleLignes.removeRow(row);
            majTotal();
        }

        private void majTotal() {
            BigDecimal total = BigDecimal.ZERO;
            for (LigneApprovisionnement l : lignes) total = total.add(l.getSousTotal());
            labelTotal.setText("Total : " + FormatUtil.montant(total));
        }

        private LocalDate parseDate(String s) {
            if (s == null || s.isBlank()) return null;
            return LocalDate.parse(s.trim(), DATE_FMT);
        }

        private void valider() {
            if (lignes.isEmpty()) {
                UIHelper.avertissement(this, "Ajoutez au moins une ligne a l'approvisionnement.");
                return;
            }
            Fournisseur f = (Fournisseur) comboFournisseur.getSelectedItem();

            Approvisionnement appro = new Approvisionnement();
            appro.setReference(service.genererReference());
            if (f != null) {
                appro.setFournisseurId(f.getId());
                appro.setFournisseurNom(f.getNom());
            }
            for (LigneApprovisionnement l : lignes) appro.ajouterLigne(l);
            appro.calculerTotal();

            try {
                service.enregistrer(appro);
                UIHelper.succes(this, "Approvisionnement enregistre. Le stock a ete augmente.");
                dispose();
                rafraichir();
            } catch (AppException ex) {
                UIHelper.erreur(this, ex.getMessage());
            }
        }
    }
}
