package com.pharmacie.tools;

import com.pharmacie.config.ConnectionManager;
import com.pharmacie.dao.MedicamentDao;
import com.pharmacie.dao.VenteDao;
import com.pharmacie.model.*;
import com.pharmacie.service.ApprovisionnementService;
import com.pharmacie.service.AuthService;
import com.pharmacie.service.DatabaseBootstrap;
import com.pharmacie.service.VenteService;
import com.pharmacie.util.AppException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.*;

/**
 * Genere un jeu de donnees de demonstration COHERENT (stock, lots, mouvements,
 * approvisionnements et ventes) en passant par les services transactionnels,
 * ce qui garantit l'integrite (stock = somme des lots, historique correct).
 *
 * Execution :
 *   java -cp target/gestion-pharmacie.jar com.pharmacie.tools.DemoDataSeeder
 *   (ajouter l'argument "force" pour regenerer meme si des ventes existent deja)
 *
 * Idempotent : ne fait rien si des ventes existent deja (sauf "force").
 */
public class DemoDataSeeder {

    // ref, quantite, prix d'achat, decalage d'expiration en jours (negatif = perime)
    private static final Object[][] APPRO_1 = {
            {"MED-0001", 120, 300, 400},
            {"MED-0002",  80, 500, 450},
            {"MED-0004", 150, 150, 600},
            {"MED-0005",  60, 200,  -5},   // lot perime (demonstration)
            {"MED-0006",  40, 350,  40},   // proche expiration
            {"MED-0007",  90, 400, 500},
            {"MED-0010",  30, 700,  25},   // proche expiration
            {"MED-0011",  70, 1000, 700},
    };
    private static final Object[][] APPRO_2 = {
            {"MED-0003",   6, 900, 300},   // stock faible
            {"MED-0008",   4, 1800, 300},  // stock faible
            {"MED-0009",  45, 600, 400},
            {"MED-0012",  25, 2500, 500},
            {"MED-0013",  40, 1100, 600},
            {"MED-0014",  55, 950, 550},
            {"MED-0015",  20, 3000, 400},
            {"MED-0016",   3, 4000, 300},  // stock faible
    };

    // Medicaments vendables (on exclut les 3 volontairement en stock faible)
    private static final String[] VENDABLES = {
            "MED-0001", "MED-0002", "MED-0004", "MED-0005", "MED-0006",
            "MED-0007", "MED-0009", "MED-0010", "MED-0011", "MED-0012",
            "MED-0013", "MED-0014"
    };

    public static void main(String[] args) {
        boolean force = args.length > 0 && args[0].equalsIgnoreCase("force");

        if (!ConnectionManager.testConnection()) {
            System.err.println("Connexion a la base impossible. Demarrez MySQL et creez la base.");
            System.exit(1);
        }
        new DatabaseBootstrap().initialiser();
        new AuthService().connexion("admin", "admin123");

        VenteDao venteDao = new VenteDao();
        if (!force && !venteDao.findAll().isEmpty()) {
            System.out.println("Des ventes existent deja : donnees de demo non regenerees "
                    + "(utilisez l'argument \"force\" pour forcer).");
            return;
        }

        MedicamentDao medDao = new MedicamentDao();
        Map<String, Medicament> byRef = new HashMap<>();
        for (Medicament m : medDao.findAll()) byRef.put(m.getReference(), m);

        // --- Approvisionnements (remplissent le stock et creent les lots) ---
        creerAppro(byRef, 1, APPRO_1, "A");
        creerAppro(byRef, 2, APPRO_2, "B");
        System.out.println("2 approvisionnements crees, stock et lots initialises.");

        // --- Ventes reparties sur les 7 derniers jours ---
        int ok = creerVentes(byRef);
        System.out.println(ok + " ventes de demonstration creees.");

        System.out.println("Donnees de demonstration generees avec succes.");
    }

    private static void creerAppro(Map<String, Medicament> byRef, int fournisseurId,
                                   Object[][] lignes, String lotPrefix) {
        Approvisionnement a = new Approvisionnement();
        a.setFournisseurId(fournisseurId);
        int i = 1;
        for (Object[] l : lignes) {
            Medicament m = byRef.get((String) l[0]);
            if (m == null) continue;
            LigneApprovisionnement la = new LigneApprovisionnement();
            la.setMedicamentId(m.getId());
            la.setMedicamentNom(m.getNom());
            la.setQuantite((int) l[1]);
            la.setPrixAchat(BigDecimal.valueOf(((Number) l[2]).longValue()));
            la.setNumeroLot("LOT-" + lotPrefix + String.format("%03d", i++));
            la.setDateExpiration(LocalDate.now().plusDays(((Number) l[3]).intValue()));
            a.ajouterLigne(la);
        }
        new ApprovisionnementService().enregistrer(a);
    }

    private static int creerVentes(Map<String, Medicament> byRef) {
        VenteService venteService = new VenteService();
        ModePaiement[] modes = ModePaiement.values();
        Random rnd = new Random(42);          // deterministe
        List<String[]> backdates = new ArrayList<>();
        int created = 0;

        for (int i = 0; i < 14; i++) {
            Vente v = new Vente();
            int nbLignes = 1 + rnd.nextInt(3);
            Set<String> choisis = new LinkedHashSet<>();
            while (choisis.size() < nbLignes) {
                choisis.add(VENDABLES[rnd.nextInt(VENDABLES.length)]);
            }
            for (String ref : choisis) {
                Medicament m = byRef.get(ref);
                if (m == null) continue;
                v.ajouterLigne(new LigneVente(m, 1 + rnd.nextInt(3)));
            }
            // Client : 2 ventes sur 3 nominatives, sinon anonyme
            if (rnd.nextInt(3) != 0) {
                v.setClientId(1 + rnd.nextInt(6));
            }
            v.setModePaiement(modes[rnd.nextInt(modes.length)]);
            try {
                Vente enr = venteService.enregistrer(v);
                created++;
                int jours = rnd.nextInt(7);          // 0..6 jours en arriere
                int heures = 8 + rnd.nextInt(10);     // entre 8h et 17h
                backdates.add(new String[]{enr.getReference(), String.valueOf(jours), String.valueOf(heures)});
            } catch (AppException ex) {
                System.out.println("  (vente ignoree : " + ex.getMessage() + ")");
            }
        }

        // Repartit les dates des ventes sur les derniers jours (pour les rapports)
        backdater(backdates);
        return created;
    }

    private static void backdater(List<String[]> backdates) {
        String sql = "UPDATE vente SET date_vente = "
                + "DATE_SUB(DATE_SUB(DATE(NOW()), INTERVAL ? DAY), INTERVAL -? HOUR) WHERE reference = ?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (String[] b : backdates) {
                ps.setInt(1, Integer.parseInt(b[1]));
                ps.setInt(2, Integer.parseInt(b[2]));
                ps.setString(3, b[0]);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (Exception e) {
            System.out.println("  (repartition des dates ignoree : " + e.getMessage() + ")");
        }
    }
}
