package com.pharmacie.view.panels;

import com.pharmacie.model.Lot;
import com.pharmacie.model.Medicament;
import com.pharmacie.model.MouvementStock;
import com.pharmacie.service.StockService;
import com.pharmacie.util.FormatUtil;
import com.pharmacie.view.components.BadgeRenderer;
import com.pharmacie.view.components.ModulePanel;
import com.pharmacie.view.components.UIFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * Module de suivi du stock : quantites disponibles, lots/expirations et
 * historique des mouvements, repartis dans trois onglets.
 */
public class StockPanel extends ModulePanel {

    private final StockService service = new StockService();

    private final DefaultTableModel modeleStock = UIFactory.modeleTable(
            "Medicament", "Reference", "Categorie", "Stock", "Seuil", "Statut");
    private final JTable tableStock = new JTable(modeleStock);

    private final DefaultTableModel modeleLots = UIFactory.modeleTable(
            "Medicament", "N Lot", "Quantite", "Prix achat", "Reception", "Expiration", "Etat");
    private final JTable tableLots = new JTable(modeleLots);

    private final DefaultTableModel modeleMouvements = UIFactory.modeleTable(
            "Date", "Medicament", "Type", "Quantite", "N Lot", "Source", "Utilisateur");
    private final JTable tableMouvements = new JTable(modeleMouvements);

    public StockPanel() {
        super("Stock", "Quantites disponibles, lots et mouvements");

        JButton btnActualiser = UIFactory.boutonSecondaire("Actualiser");
        btnActualiser.addActionListener(e -> rafraichir());
        ajouterAction(btnActualiser);

        JScrollPane spStock = UIFactory.tableStylisee(tableStock);
        JScrollPane spLots = UIFactory.tableStylisee(tableLots);
        JScrollPane spMouvements = UIFactory.tableStylisee(tableMouvements);

        // Pastilles de statut colorees
        tableStock.getColumnModel().getColumn(5).setCellRenderer(new BadgeRenderer());
        tableLots.getColumnModel().getColumn(6).setCellRenderer(new BadgeRenderer());

        JTabbedPane onglets = new JTabbedPane();
        onglets.addTab("Stock actuel", spStock);
        onglets.addTab("Lots & expirations", spLots);
        onglets.addTab("Mouvements", spMouvements);

        setContenu(onglets);
    }

    @Override
    public void rafraichir() {
        chargerStock();
        chargerLots();
        chargerMouvements();
    }

    private void chargerStock() {
        List<Medicament> meds = service.stockActuel();
        modeleStock.setRowCount(0);
        for (Medicament m : meds) {
            modeleStock.addRow(new Object[]{
                    m.getNom(),
                    m.getReference(),
                    m.getCategorieNom() == null ? "-" : m.getCategorieNom(),
                    m.getQuantiteStock(),
                    m.getSeuilStock(),
                    m.isStockFaible() ? "STOCK FAIBLE" : "OK"});
        }
    }

    private void chargerLots() {
        List<Lot> lots = service.tousLesLots();
        modeleLots.setRowCount(0);
        for (Lot l : lots) {
            String etat = l.isExpire() ? "EXPIRE"
                    : (l.isProcheExpiration(60) ? "PROCHE" : "OK");
            modeleLots.addRow(new Object[]{
                    l.getMedicamentNom(),
                    l.getNumeroLot(),
                    l.getQuantite(),
                    FormatUtil.montant(l.getPrixAchat()),
                    FormatUtil.date(l.getDateReception()),
                    FormatUtil.date(l.getDateExpiration()),
                    etat});
        }
    }

    private void chargerMouvements() {
        List<MouvementStock> mouvements = service.mouvementsRecents(200);
        modeleMouvements.setRowCount(0);
        for (MouvementStock mv : mouvements) {
            modeleMouvements.addRow(new Object[]{
                    FormatUtil.dateHeure(mv.getDateMouvement()),
                    mv.getMedicamentNom(),
                    mv.getTypeMouvement() == null ? "-" : mv.getTypeMouvement().toString(),
                    mv.getQuantite(),
                    mv.getNumeroLot(),
                    mv.getSource(),
                    mv.getUtilisateurNom()});
        }
    }
}
