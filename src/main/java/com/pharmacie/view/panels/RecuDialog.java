package com.pharmacie.view.panels;

import com.pharmacie.model.LigneVente;
import com.pharmacie.model.Vente;
import com.pharmacie.util.FormatUtil;
import com.pharmacie.util.PharmacieInfo;
import com.pharmacie.util.UIHelper;
import com.pharmacie.view.components.UIFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PrinterException;

/**
 * Boite de dialogue affichant le recu (ticket de caisse) d'une vente.
 * Le ticket est rendu dans un JTextArea monospace et peut etre imprime.
 */
public class RecuDialog extends JDialog {

    /** Largeur fixe du ticket, en caracteres. */
    private static final int LARGEUR = 42;

    private final JTextArea zone = new JTextArea();

    public RecuDialog(Window parent, Vente vente) {
        super(parent, "Recu de vente", ModalityType.APPLICATION_MODAL);

        zone.setFont(new Font("Monospaced", Font.PLAIN, 12));
        zone.setEditable(false);
        zone.setColumns(LARGEUR);
        zone.setMargin(new Insets(10, 12, 10, 12));
        zone.setText(construireTicket(vente));
        zone.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(zone);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        boutons.setBackground(Color.WHITE);
        JButton imprimer = UIFactory.boutonPrincipal("Imprimer");
        imprimer.addActionListener(e -> imprimer());
        JButton fermer = UIFactory.boutonSecondaire("Fermer");
        fermer.addActionListener(e -> dispose());
        boutons.add(imprimer);
        boutons.add(fermer);

        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);
        add(boutons, BorderLayout.SOUTH);
        setSize(380, 560);
        setLocationRelativeTo(parent);
    }

    private void imprimer() {
        try {
            boolean imprime = zone.print();
            if (imprime) {
                UIHelper.info(this, "Impression lancee.");
            }
        } catch (PrinterException ex) {
            UIHelper.erreur(this, "Impossible d'imprimer le recu : " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------ CONSTRUCTION

    private String construireTicket(Vente vente) {
        StringBuilder sb = new StringBuilder();

        // En-tete centre
        sb.append(centrer(PharmacieInfo.NOM)).append('\n');
        sb.append(centrer(PharmacieInfo.ADRESSE)).append('\n');
        sb.append(centrer(PharmacieInfo.TELEPHONE)).append('\n');
        sb.append(centrer(PharmacieInfo.EMAIL)).append('\n');
        sb.append(separateur()).append('\n');

        // Infos vente
        sb.append("No recu : ").append(nn(vente.getReference())).append('\n');
        sb.append("Date    : ").append(FormatUtil.dateHeure(vente.getDateVente())).append('\n');
        sb.append("Vendeur : ").append(nn(vente.getUtilisateurNom())).append('\n');
        sb.append("Client  : ").append(nn(vente.getClientNom())).append('\n');
        sb.append(separateur()).append('\n');

        // Lignes
        for (LigneVente l : vente.getLignes()) {
            sb.append(tronquer(nn(l.getMedicamentNom()), LARGEUR)).append('\n');
            String gauche = "  " + l.getQuantite() + " x " + FormatUtil.montant(l.getPrixUnitaire());
            String droite = FormatUtil.montant(l.getSousTotal());
            sb.append(aligner(gauche, droite)).append('\n');
        }
        sb.append(separateur()).append('\n');

        // Totaux
        sb.append(aligner("TOTAL :", FormatUtil.montant(vente.getMontantTotal()))).append('\n');
        sb.append("Mode de paiement : ").append(
                vente.getModePaiement() == null ? "-" : vente.getModePaiement().toString()).append('\n');
        sb.append("Nombre d'articles : ").append(vente.getNombreArticles()).append('\n');
        sb.append(separateur()).append('\n');

        // Message de remerciement centre (gere le retour a la ligne si trop long)
        for (String ligne : enveloppes(PharmacieInfo.MESSAGE_MERCI)) {
            sb.append(centrer(ligne)).append('\n');
        }

        return sb.toString();
    }

    // ------------------------------------------------------------ FORMATAGE

    private String separateur() {
        return "-".repeat(LARGEUR);
    }

    /** Centre un texte sur la largeur fixe du ticket. */
    private String centrer(String texte) {
        String t = nn(texte);
        if (t.length() >= LARGEUR) return tronquer(t, LARGEUR);
        int espaces = (LARGEUR - t.length()) / 2;
        return " ".repeat(espaces) + t;
    }

    /** Place {@code gauche} a gauche et {@code droite} a droite sur la largeur. */
    private String aligner(String gauche, String droite) {
        String g = nn(gauche);
        String d = nn(droite);
        int libre = LARGEUR - g.length() - d.length();
        if (libre < 1) {
            // Pas assez de place : on renvoie gauche + espace + droite
            return g + " " + d;
        }
        return g + " ".repeat(libre) + d;
    }

    private String tronquer(String texte, int max) {
        String t = nn(texte);
        return t.length() <= max ? t : t.substring(0, max);
    }

    /** Decoupe un texte en lignes qui tiennent dans la largeur du ticket. */
    private java.util.List<String> enveloppes(String texte) {
        java.util.List<String> lignes = new java.util.ArrayList<>();
        String t = nn(texte).trim();
        if (t.isEmpty()) {
            lignes.add("");
            return lignes;
        }
        StringBuilder courante = new StringBuilder();
        for (String mot : t.split("\\s+")) {
            if (courante.length() == 0) {
                courante.append(mot);
            } else if (courante.length() + 1 + mot.length() <= LARGEUR) {
                courante.append(' ').append(mot);
            } else {
                lignes.add(courante.toString());
                courante = new StringBuilder(mot);
            }
        }
        if (courante.length() > 0) lignes.add(courante.toString());
        return lignes;
    }

    private String nn(String s) {
        return s == null ? "" : s;
    }
}
