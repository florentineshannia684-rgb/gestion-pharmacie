package com.pharmacie.view.panels;

import com.pharmacie.model.Vente;
import com.pharmacie.service.VenteService;
import com.pharmacie.util.FormatUtil;
import com.pharmacie.util.UIHelper;
import com.pharmacie.view.components.ModulePanel;
import com.pharmacie.view.components.UIFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * Historique des ventes : liste, recherche et consultation du recu.
 */
public class HistoriqueVentePanel extends ModulePanel {

    private final VenteService venteService = new VenteService();

    private final JTextField champRecherche =
            UIFactory.champRecherche("Rechercher par reference ou client...");
    private final DefaultTableModel modele = UIFactory.modeleTable(
            "ID", "Reference", "Date", "Client", "Vendeur", "Articles", "Montant");
    private final JTable table = new JTable(modele);

    private List<Vente> donnees;

    public HistoriqueVentePanel() {
        super("Historique des ventes", "Consultation des ventes enregistrees");

        champRecherche.addActionListener(e -> rafraichir());
        UIFactory.liveSearch(champRecherche, this::rafraichir);

        JButton btnRecu = UIFactory.boutonPrincipal("Voir le recu");
        btnRecu.addActionListener(e -> voirRecu());
        JButton btnRafraichir = UIFactory.boutonSecondaire("Rafraichir");
        btnRafraichir.addActionListener(e -> rafraichir());

        ajouterAction(champRecherche);
        ajouterAction(btnRecu);
        ajouterAction(btnRafraichir);

        table.getColumnModel().getColumn(0).setMaxWidth(44);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) voirRecu();
            }
        });

        setContenu(UIFactory.tableStylisee(table));
    }

    @Override
    public void rafraichir() {
        String terme = champRecherche.getText().trim();
        donnees = terme.isBlank() ? venteService.lister() : venteService.rechercher(terme);
        modele.setRowCount(0);
        for (Vente v : donnees) {
            modele.addRow(new Object[]{
                    v.getId(),
                    v.getReference(),
                    FormatUtil.dateHeure(v.getDateVente()),
                    v.getClientNom() == null ? "-" : v.getClientNom(),
                    v.getUtilisateurNom() == null ? "-" : v.getUtilisateurNom(),
                    v.getNombreArticles(),
                    FormatUtil.montant(v.getMontantTotal())});
        }
        setSousTitre(donnees.size() + " vente(s)");
    }

    private void voirRecu() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIHelper.avertissement(this, "Selectionnez une vente pour afficher son recu.");
            return;
        }
        int id = (int) modele.getValueAt(row, 0);
        Vente v = venteService.parId(id);
        if (v == null) {
            UIHelper.erreur(this, "Vente introuvable.");
            return;
        }
        new RecuDialog(SwingUtilities.getWindowAncestor(this), v).setVisible(true);
    }
}
