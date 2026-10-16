package com.pharmacie.view.panels;

import com.pharmacie.model.Client;
import com.pharmacie.model.LigneVente;
import com.pharmacie.model.Medicament;
import com.pharmacie.model.ModePaiement;
import com.pharmacie.model.Vente;
import com.pharmacie.service.ClientService;
import com.pharmacie.service.MedicamentService;
import com.pharmacie.service.VenteService;
import com.pharmacie.util.AppException;
import com.pharmacie.util.FormatUtil;
import com.pharmacie.util.UIHelper;
import com.pharmacie.util.UITheme;
import com.pharmacie.view.components.ModulePanel;
import com.pharmacie.view.components.UIFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Interface de caisse optimisee :
 *  - recherche instantanee (filtrage en memoire) ;
 *  - ajout au panier en un clic / double-clic / touche Entree (quantite 1) ;
 *  - quantite modifiable directement dans le panier (boutons +/- ou saisie) ;
 *  - controle du stock en temps reel ;
 *  - validation transactionnelle de la vente et generation du recu.
 */
public class VentePanel extends ModulePanel {

    private final MedicamentService medicamentService = new MedicamentService();
    private final ClientService clientService = new ClientService();
    private final VenteService venteService = new VenteService();

    // --- Catalogue (gauche) ---
    private final JTextField champRecherche =
            UIFactory.champRecherche("Rechercher un medicament (nom ou reference)...");
    private final DefaultTableModel modeleCatalogue =
            UIFactory.modeleTable("Reference", "Nom", "Prix", "Stock");
    private final JTable tableCatalogue = new JTable(modeleCatalogue);
    private List<Medicament> tousMedicaments = new ArrayList<>();   // tout le catalogue
    private List<Medicament> catalogue = new ArrayList<>();         // filtre affiche
    private final Map<Integer, Integer> stockParMed = new HashMap<>();

    // --- Panier (droite) ---
    private final List<LigneVente> panier = new ArrayList<>();
    private final DefaultTableModel modelePanier = new DefaultTableModel(
            new Object[]{"Medicament", "Qte", "PU", "Sous-total"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return column == 1; }
    };
    private final JTable tablePanier = new JTable(modelePanier);
    private boolean majPanierEnCours = false;

    private final JLabel labelTotal = new JLabel();
    private final JLabel labelArticles = new JLabel();
    private final JComboBox<Object> comboClient = new JComboBox<>();
    private final JComboBox<ModePaiement> comboPaiement = new JComboBox<>(ModePaiement.values());

    private static final String CLIENT_ANONYME = "Client anonyme";

    public VentePanel() {
        super("Ventes / Caisse", "Enregistrement des ventes");

        // Recherche instantanee (pas besoin d'appuyer sur Entree)
        UIFactory.liveSearch(champRecherche, this::filtrerCatalogue);

        configurerTableCatalogue();
        configurerTablePanier();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                construireCatalogue(), construirePanier());
        split.setResizeWeight(0.52);
        split.setBorder(null);
        split.setOpaque(false);
        setContenu(split);

        rafraichir();
    }

    // ------------------------------------------------------------ CONFIG TABLES

    private void configurerTableCatalogue() {
        tableCatalogue.getColumnModel().getColumn(0).setPreferredWidth(95);
        tableCatalogue.getColumnModel().getColumn(0).setMinWidth(85);
        tableCatalogue.getColumnModel().getColumn(2).setPreferredWidth(90);
        tableCatalogue.getColumnModel().getColumn(3).setPreferredWidth(70);
        tableCatalogue.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Double-clic = ajouter
        tableCatalogue.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) ajouterSelection();
            }
        });
        // Touche Entree = ajouter
        tableCatalogue.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "ajouter");
        tableCatalogue.getActionMap().put("ajouter", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { ajouterSelection(); }
        });
    }

    private void configurerTablePanier() {
        tablePanier.getColumnModel().getColumn(1).setMaxWidth(70);
        tablePanier.getColumnModel().getColumn(1).setMinWidth(50);
        tablePanier.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        // Saisie directe de la quantite dans le panier
        modelePanier.addTableModelListener(e -> {
            if (majPanierEnCours || e.getColumn() != 1) return;
            int row = e.getFirstRow();
            if (row < 0 || row >= panier.size()) return;
            Object val = modelePanier.getValueAt(row, 1);
            int nouvelle;
            try {
                nouvelle = Integer.parseInt(val.toString().trim());
            } catch (NumberFormatException ex) {
                rafraichirPanier();
                return;
            }
            appliquerQuantite(row, nouvelle);
        });
    }

    // ------------------------------------------------------------ CONSTRUCTION UI

    private JPanel construireCatalogue() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);

        JPanel haut = new JPanel(new BorderLayout(8, 0));
        haut.setOpaque(false);
        JLabel titre = UIFactory.labelChamp("Catalogue");
        haut.add(titre, BorderLayout.WEST);
        haut.add(champRecherche, BorderLayout.CENTER);
        JButton btnAjouter = UIFactory.boutonPrincipal("Ajouter au panier");
        btnAjouter.addActionListener(e -> ajouterSelection());
        haut.add(btnAjouter, BorderLayout.EAST);

        JLabel aide = new JLabel("Astuce : double-cliquez (ou touche Entree) sur un medicament pour l'ajouter.");
        aide.setFont(UITheme.FONT_SMALL);
        aide.setForeground(UITheme.TEXT_MUTED);

        JPanel centre = new JPanel(new BorderLayout(0, 6));
        centre.setOpaque(false);
        centre.add(UIFactory.tableStylisee(tableCatalogue), BorderLayout.CENTER);
        centre.add(aide, BorderLayout.SOUTH);

        p.add(haut, BorderLayout.NORTH);
        p.add(centre, BorderLayout.CENTER);
        return p;
    }

    private JPanel construirePanier() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        // En-tete panier : titre + boutons +/- + retirer
        JPanel enTete = new JPanel(new BorderLayout());
        enTete.setOpaque(false);
        enTete.add(UIFactory.labelChamp("Panier"), BorderLayout.WEST);
        JPanel actionsPanier = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionsPanier.setOpaque(false);
        JButton btnMoins = petitBouton("-");
        btnMoins.addActionListener(e -> ajusterSelection(-1));
        JButton btnPlus = petitBouton("+");
        btnPlus.addActionListener(e -> ajusterSelection(+1));
        JButton btnRetirer = UIFactory.boutonSecondaire("Retirer ligne");
        btnRetirer.addActionListener(e -> retirerLigne());
        actionsPanier.add(btnMoins);
        actionsPanier.add(btnPlus);
        actionsPanier.add(btnRetirer);
        enTete.add(actionsPanier, BorderLayout.EAST);

        p.add(enTete, BorderLayout.NORTH);
        p.add(UIFactory.tableStylisee(tablePanier), BorderLayout.CENTER);

        // Bas : total + articles + options + boutons principaux
        JPanel bas = new JPanel();
        bas.setOpaque(false);
        bas.setLayout(new BoxLayout(bas, BoxLayout.Y_AXIS));

        JPanel ligneTotal = new JPanel(new BorderLayout());
        ligneTotal.setOpaque(false);
        ligneTotal.setAlignmentX(LEFT_ALIGNMENT);
        ligneTotal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        labelTotal.setFont(UITheme.FONT_STAT);
        labelTotal.setForeground(UITheme.PRIMARY);
        labelArticles.setFont(UITheme.FONT_SMALL);
        labelArticles.setForeground(UITheme.TEXT_MUTED);
        ligneTotal.add(labelTotal, BorderLayout.WEST);
        ligneTotal.add(labelArticles, BorderLayout.EAST);

        JPanel options = new JPanel(new GridBagLayout());
        options.setOpaque(false);
        options.setAlignmentX(LEFT_ALIGNMENT);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        options.add(UIFactory.labelChamp("Client"), g);
        g.gridx = 1; g.weightx = 1;
        options.add(comboClient, g);
        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        options.add(UIFactory.labelChamp("Paiement"), g);
        g.gridx = 1; g.weightx = 1;
        options.add(comboPaiement, g);

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        boutons.setOpaque(false);
        boutons.setAlignmentX(LEFT_ALIGNMENT);
        JButton btnVider = UIFactory.boutonDanger("Vider");
        btnVider.addActionListener(e -> viderPanier());
        JButton btnValider = UIFactory.boutonSucces("Valider la vente");
        btnValider.addActionListener(e -> validerVente());
        boutons.add(btnVider);
        boutons.add(btnValider);

        bas.add(ligneTotal);
        bas.add(Box.createVerticalStrut(8));
        bas.add(options);
        bas.add(Box.createVerticalStrut(8));
        bas.add(boutons);

        p.add(bas, BorderLayout.SOUTH);
        return p;
    }

    private JButton petitBouton(String texte) {
        JButton b = new JButton(texte);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setForeground(Color.WHITE);
        b.setBackground(UITheme.PRIMARY);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ------------------------------------------------------------ CATALOGUE

    /** Filtre le catalogue deja charge en memoire (recherche instantanee). */
    private void filtrerCatalogue() {
        String terme = champRecherche.getText().trim().toLowerCase(Locale.ROOT);
        catalogue = new ArrayList<>();
        for (Medicament m : tousMedicaments) {
            if (terme.isEmpty()
                    || (m.getNom() != null && m.getNom().toLowerCase(Locale.ROOT).contains(terme))
                    || (m.getReference() != null && m.getReference().toLowerCase(Locale.ROOT).contains(terme))) {
                catalogue.add(m);
            }
        }
        modeleCatalogue.setRowCount(0);
        for (Medicament m : catalogue) {
            modeleCatalogue.addRow(new Object[]{
                    m.getReference(),
                    m.getNom(),
                    FormatUtil.montant(m.getPrixVente()),
                    m.getQuantiteStock() + (m.isStockFaible() ? " !" : "")});
        }
    }

    private void chargerCatalogue() {
        tousMedicaments = medicamentService.rechercher("", null);
        stockParMed.clear();
        for (Medicament m : tousMedicaments) stockParMed.put(m.getId(), m.getQuantiteStock());
        filtrerCatalogue();
    }

    private Medicament medicamentSelectionne() {
        int row = tableCatalogue.getSelectedRow();
        if (row < 0 || row >= catalogue.size()) return null;
        return catalogue.get(row);
    }

    /** Ajoute le medicament selectionne au panier (quantite 1, sans boite de dialogue). */
    private void ajouterSelection() {
        Medicament m = medicamentSelectionne();
        if (m == null) {
            UIHelper.avertissement(this, "Selectionnez un medicament dans le catalogue.");
            return;
        }
        int stock = stockParMed.getOrDefault(m.getId(), 0);
        LigneVente existante = trouverLigne(m.getId());
        int actuelle = existante == null ? 0 : existante.getQuantite();
        if (actuelle + 1 > stock) {
            UIHelper.avertissement(this, "Stock insuffisant pour \"" + m.getNom()
                    + "\" (disponible : " + stock + ").");
            return;
        }
        if (existante != null) {
            existante.setQuantite(existante.getQuantite() + 1);
        } else {
            panier.add(new LigneVente(m, 1));
        }
        rafraichirPanier();
        selectionnerLigne(m.getId());
    }

    private LigneVente trouverLigne(int medicamentId) {
        for (LigneVente l : panier) {
            if (l.getMedicamentId() == medicamentId) return l;
        }
        return null;
    }

    // ------------------------------------------------------------ PANIER

    /** Ajuste la quantite de la ligne selectionnee de +/- delta. */
    private void ajusterSelection(int delta) {
        int row = tablePanier.getSelectedRow();
        if (row < 0 || row >= panier.size()) {
            UIHelper.avertissement(this, "Selectionnez une ligne du panier.");
            return;
        }
        appliquerQuantite(row, panier.get(row).getQuantite() + delta);
    }

    /** Applique une nouvelle quantite a une ligne, avec controle du stock. */
    private void appliquerQuantite(int row, int nouvelle) {
        LigneVente ligne = panier.get(row);
        if (nouvelle <= 0) {
            panier.remove(row);
            rafraichirPanier();
            return;
        }
        int stock = stockParMed.getOrDefault(ligne.getMedicamentId(), 0);
        if (nouvelle > stock) {
            UIHelper.avertissement(this, "Stock insuffisant pour \"" + ligne.getMedicamentNom()
                    + "\" (disponible : " + stock + ").");
            rafraichirPanier();
            return;
        }
        ligne.setQuantite(nouvelle);
        rafraichirPanier();
        selectionnerLigne(ligne.getMedicamentId());
    }

    private void selectionnerLigne(int medicamentId) {
        for (int i = 0; i < panier.size(); i++) {
            if (panier.get(i).getMedicamentId() == medicamentId) {
                tablePanier.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    private void rafraichirPanier() {
        majPanierEnCours = true;
        modelePanier.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        int articles = 0;
        for (LigneVente l : panier) {
            modelePanier.addRow(new Object[]{
                    l.getMedicamentNom(),
                    l.getQuantite(),
                    FormatUtil.montant(l.getPrixUnitaire()),
                    FormatUtil.montant(l.getSousTotal())});
            total = total.add(l.getSousTotal());
            articles += l.getQuantite();
        }
        labelTotal.setText("TOTAL : " + FormatUtil.montant(total));
        labelArticles.setText(articles + " article(s) - " + panier.size() + " ligne(s)   ");
        majPanierEnCours = false;
    }

    private void retirerLigne() {
        int row = tablePanier.getSelectedRow();
        if (row < 0 || row >= panier.size()) {
            UIHelper.avertissement(this, "Selectionnez une ligne du panier a retirer.");
            return;
        }
        panier.remove(row);
        rafraichirPanier();
    }

    private void viderPanier() {
        if (panier.isEmpty()) return;
        if (UIHelper.confirmer(this, "Vider le panier ?")) {
            panier.clear();
            rafraichirPanier();
        }
    }

    // ------------------------------------------------------------ VALIDATION

    private void validerVente() {
        if (panier.isEmpty()) {
            UIHelper.avertissement(this, "Le panier est vide. Ajoutez au moins un medicament.");
            return;
        }

        Vente vente = new Vente();
        for (LigneVente l : panier) {
            LigneVente copie = new LigneVente();
            copie.setMedicamentId(l.getMedicamentId());
            copie.setMedicamentNom(l.getMedicamentNom());
            copie.setReference(l.getReference());
            copie.setPrixUnitaire(l.getPrixUnitaire());
            copie.setQuantite(l.getQuantite());
            vente.ajouterLigne(copie);
        }

        Object clientSel = comboClient.getSelectedItem();
        if (clientSel instanceof Client c) {
            vente.setClientId(c.getId());
            vente.setClientNom(c.getNomComplet().trim());
        } else {
            vente.setClientId(null);
            vente.setClientNom(CLIENT_ANONYME);
        }
        vente.setModePaiement((ModePaiement) comboPaiement.getSelectedItem());

        try {
            Vente enregistree = venteService.enregistrer(vente);
            panier.clear();
            rafraichirPanier();
            chargerCatalogue();
            UIHelper.succes(this, "Vente " + enregistree.getReference() + " enregistree.");
            new RecuDialog(SwingUtilities.getWindowAncestor(this), enregistree).setVisible(true);
        } catch (AppException ex) {
            UIHelper.erreur(this, ex.getMessage());
            chargerCatalogue();
        }
    }

    // ------------------------------------------------------------ RAFRAICHIR

    private void chargerClients() {
        Object prec = comboClient.getSelectedItem();
        comboClient.removeAllItems();
        comboClient.addItem(CLIENT_ANONYME);
        for (Client c : clientService.lister()) comboClient.addItem(c);
        if (prec != null) comboClient.setSelectedItem(prec);
    }

    @Override
    public void rafraichir() {
        chargerCatalogue();
        chargerClients();
        rafraichirPanier();
    }
}
