package com.pharmacie.view.panels;

import com.pharmacie.service.StatistiquesService;
import com.pharmacie.util.FormatUtil;
import com.pharmacie.util.UIHelper;
import com.pharmacie.util.UITheme;
import com.pharmacie.view.components.ModulePanel;
import com.pharmacie.view.components.StatCard;
import com.pharmacie.view.components.UIFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Module des rapports et statistiques : indicateurs et tableaux sur une
 * periode choisie (debut / fin).
 */
public class RapportPanel extends ModulePanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final StatistiquesService service = new StatistiquesService();

    private final JTextField champDebut = UIFactory.champTexte();
    private final JTextField champFin = UIFactory.champTexte();

    private final StatCard cardVentes = new StatCard("Ventes (periode)", "0", "", UITheme.CARD_BLUE);
    private final StatCard cardCA = new StatCard("Chiffre d'affaires", FormatUtil.montant(BigDecimal.ZERO), "", UITheme.CARD_GREEN);
    private final StatCard cardMedicaments = new StatCard("Medicaments en stock", "0", "", UITheme.CARD_PURPLE);
    private final StatCard cardStockFaible = new StatCard("Stock faible", "0", "", UITheme.CARD_ORANGE);
    private final StatCard cardExpiration = new StatCard("Expirations proches", "0", "", UITheme.CARD_RED);

    private final DefaultTableModel modeleTop = UIFactory.modeleTable("Medicament", "Quantite", "Montant");
    private final JTable tableTop = new JTable(modeleTop);

    private final DefaultTableModel modeleCA = UIFactory.modeleTable("Jour", "Montant");
    private final JTable tableCA = new JTable(modeleCA);

    public RapportPanel() {
        super("Rapports & statistiques", "Indicateurs sur la periode selectionnee");

        champDebut.setColumns(10);
        champFin.setColumns(10);

        JButton btnGenerer = UIFactory.boutonPrincipal("Generer");
        btnGenerer.addActionListener(e -> generer());

        ajouterAction(UIFactory.labelChamp("Du"));
        ajouterAction(champDebut);
        ajouterAction(UIFactory.labelChamp("Au"));
        ajouterAction(champFin);
        ajouterAction(btnGenerer);

        setContenu(new JScrollPane(construireCorps()));
    }

    private JPanel construireCorps() {
        JPanel corps = new JPanel();
        corps.setBackground(UITheme.BG);
        corps.setLayout(new BoxLayout(corps, BoxLayout.Y_AXIS));

        JPanel cartes = new JPanel(new GridLayout(1, 5, 12, 0));
        cartes.setBackground(UITheme.BG);
        cartes.add(cardVentes);
        cartes.add(cardCA);
        cartes.add(cardMedicaments);
        cartes.add(cardStockFaible);
        cartes.add(cardExpiration);
        cartes.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane spTop = UIFactory.tableStylisee(tableTop);
        spTop.setAlignmentX(Component.LEFT_ALIGNMENT);
        JScrollPane spCA = UIFactory.tableStylisee(tableCA);
        spCA.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titreTop = UIFactory.titre("Meilleures ventes");
        titreTop.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel titreCA = UIFactory.titre("Chiffre d'affaires par jour");
        titreCA.setAlignmentX(Component.LEFT_ALIGNMENT);

        corps.add(cartes);
        corps.add(Box.createVerticalStrut(18));
        corps.add(titreTop);
        corps.add(Box.createVerticalStrut(6));
        corps.add(spTop);
        corps.add(Box.createVerticalStrut(18));
        corps.add(titreCA);
        corps.add(Box.createVerticalStrut(6));
        corps.add(spCA);
        return corps;
    }

    @Override
    public void rafraichir() {
        LocalDate debut = LocalDate.now().withDayOfMonth(1);
        LocalDate fin = LocalDate.now();
        champDebut.setText(debut.format(DATE_FMT));
        champFin.setText(fin.format(DATE_FMT));
        calculer(debut, fin);
    }

    private void generer() {
        LocalDate debut;
        LocalDate fin;
        try {
            debut = LocalDate.parse(champDebut.getText().trim(), DATE_FMT);
            fin = LocalDate.parse(champFin.getText().trim(), DATE_FMT);
        } catch (RuntimeException ex) {
            UIHelper.erreur(this, "Date invalide (jj/MM/aaaa)");
            return;
        }
        calculer(debut, fin);
    }

    private void calculer(LocalDate debut, LocalDate fin) {
        String periode = debut.format(DATE_FMT) + " - " + fin.format(DATE_FMT);

        cardVentes.setValeur(String.valueOf(service.nbVentes(debut, fin)));
        cardVentes.setSousTitre(periode);
        cardCA.setValeur(FormatUtil.montant(service.chiffreAffaires(debut, fin)));
        cardCA.setSousTitre(periode);
        cardMedicaments.setValeur(String.valueOf(service.nbMedicaments()));
        cardStockFaible.setValeur(String.valueOf(service.nbStockFaible()));
        cardExpiration.setValeur(String.valueOf(service.nbProchesExpiration()));

        modeleTop.setRowCount(0);
        List<Object[]> top = service.topMedicaments(debut, fin, 10);
        for (Object[] r : top) {
            modeleTop.addRow(new Object[]{
                    r[0],
                    r[1],
                    FormatUtil.montant((BigDecimal) r[2])});
        }

        modeleCA.setRowCount(0);
        List<Object[]> ca = service.chiffreAffairesParJour(debut, fin);
        for (Object[] r : ca) {
            modeleCA.addRow(new Object[]{
                    FormatUtil.date((LocalDate) r[0]),
                    FormatUtil.montant((BigDecimal) r[1])});
        }

        setSousTitre("Periode : " + periode);
    }
}
