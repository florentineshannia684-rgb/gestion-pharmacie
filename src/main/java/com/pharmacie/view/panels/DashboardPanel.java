package com.pharmacie.view.panels;

import com.pharmacie.model.Lot;
import com.pharmacie.model.Medicament;
import com.pharmacie.service.StatistiquesService;
import com.pharmacie.service.StockService;
import com.pharmacie.util.FormatUtil;
import com.pharmacie.util.Session;
import com.pharmacie.util.UITheme;
import com.pharmacie.view.MainFrame;
import com.pharmacie.view.components.ModulePanel;
import com.pharmacie.view.components.StatCard;
import com.pharmacie.view.components.UIFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Tableau de bord : cartes statistiques + listes stock faible et expirations.
 */
public class DashboardPanel extends ModulePanel {

    private final StatistiquesService statsService = new StatistiquesService();
    private final StockService stockService = new StockService();
    private final MainFrame mainFrame;

    private final StatCard carteMedicaments = new StatCard("Medicaments", "0", "references en catalogue", UITheme.CARD_BLUE);
    private final StatCard carteStockFaible = new StatCard("Stock faible", "0", "a reapprovisionner", UITheme.CARD_ORANGE);
    private final StatCard carteExpiration = new StatCard("Expirations proches", "0", "lots a surveiller", UITheme.CARD_RED);
    private final StatCard carteVentesJour = new StatCard("Ventes du jour", "0", "transactions", UITheme.CARD_GREEN);
    private final StatCard carteCaJour = new StatCard("Chiffre d'affaires (jour)", "0", "aujourd'hui", UITheme.CARD_PURPLE);
    private final StatCard carteClients = new StatCard("Clients", "0", "enregistres", UITheme.ACCENT);

    private final DefaultTableModel modeleStockFaible =
            UIFactory.modeleTable("Medicament", "Reference", "Stock", "Seuil");
    private final DefaultTableModel modeleExpiration =
            UIFactory.modeleTable("Medicament", "N Lot", "Quantite", "Expiration", "Jours restants");

    public DashboardPanel(MainFrame mainFrame) {
        super("Tableau de bord", "Vue d'ensemble de l'activite de la pharmacie");
        this.mainFrame = mainFrame;

        JButton btnActualiser = UIFactory.boutonSecondaire("Actualiser");
        btnActualiser.addActionListener(e -> rafraichir());
        ajouterAction(btnActualiser);

        setContenu(construire());
    }

    private JComponent construire() {
        JPanel centre = new JPanel();
        centre.setOpaque(false);
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));

        // --- Grille de cartes ---
        JPanel grille = new JPanel(new GridLayout(2, 3, 14, 14));
        grille.setOpaque(false);
        grille.add(carteMedicaments);
        grille.add(carteStockFaible);
        grille.add(carteExpiration);
        grille.add(carteVentesJour);
        grille.add(carteCaJour);
        grille.add(carteClients);
        grille.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        grille.setAlignmentX(LEFT_ALIGNMENT);
        centre.add(grille);
        centre.add(Box.createVerticalStrut(16));

        // --- Deux tableaux cote a cote ---
        JPanel tables = new JPanel(new GridLayout(1, 2, 14, 0));
        tables.setOpaque(false);
        tables.setAlignmentX(LEFT_ALIGNMENT);

        JTable tStockFaible = new JTable(modeleStockFaible);
        tStockFaible.getColumnModel().getColumn(2).setCellRenderer(
                new com.pharmacie.view.components.BadgeRenderer(v -> {
                    try { return Integer.parseInt(v.trim()) <= 0 ? UITheme.DANGER : UITheme.WARNING; }
                    catch (Exception ex) { return null; }
                }));
        JTable tExpiration = new JTable(modeleExpiration);
        tExpiration.getColumnModel().getColumn(4).setCellRenderer(
                new com.pharmacie.view.components.BadgeRenderer(v ->
                        v != null && v.contains("EXPIRE") ? UITheme.DANGER : UITheme.WARNING));

        tables.add(bloc("Medicaments en stock faible", tStockFaible));
        tables.add(bloc("Lots proches de l'expiration", tExpiration));
        centre.add(tables);

        return new JScrollPane(centre) {{
            setBorder(null);
            getVerticalScrollBar().setUnitIncrement(16);
            getViewport().setOpaque(false);
            setOpaque(false);
        }};
    }

    private JPanel bloc(String titre, JTable table) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        JLabel t = new JLabel(titre);
        t.setFont(UITheme.FONT_SUBTITLE);
        t.setForeground(UITheme.TEXT);
        p.add(t, BorderLayout.NORTH);
        p.add(UIFactory.tableStylisee(table), BorderLayout.CENTER);
        return p;
    }

    @Override
    public void rafraichir() {
        // Cartes
        carteMedicaments.setValeur(String.valueOf(statsService.nbMedicaments()));
        int sf = statsService.nbStockFaible();
        carteStockFaible.setValeur(String.valueOf(sf));
        carteExpiration.setValeur(String.valueOf(statsService.nbProchesExpiration()));
        carteVentesJour.setValeur(String.valueOf(statsService.nbVentesDuJour()));
        carteCaJour.setValeur(FormatUtil.montant(statsService.chiffreAffairesDuJour()));
        carteClients.setValeur(String.valueOf(statsService.nbClients()));

        // Table stock faible
        modeleStockFaible.setRowCount(0);
        List<Medicament> faibles = stockService.stockFaible();
        for (Medicament m : faibles) {
            modeleStockFaible.addRow(new Object[]{
                    m.getNom(), m.getReference(), m.getQuantiteStock(), m.getSeuilStock()});
        }
        if (faibles.isEmpty()) {
            modeleStockFaible.addRow(new Object[]{"Aucun", "-", "-", "-"});
        }

        // Table expirations
        modeleExpiration.setRowCount(0);
        List<Lot> lots = stockService.lotsProchesExpiration();
        for (Lot l : lots) {
            long jours = l.joursAvantExpiration();
            modeleExpiration.addRow(new Object[]{
                    l.getMedicamentNom(), l.getNumeroLot(), l.getQuantite(),
                    FormatUtil.date(l.getDateExpiration()),
                    l.isExpire() ? "EXPIRE" : jours + " j"});
        }
        if (lots.isEmpty()) {
            modeleExpiration.addRow(new Object[]{"Aucun", "-", "-", "-", "-"});
        }
    }
}
