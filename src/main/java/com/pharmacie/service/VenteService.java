package com.pharmacie.service;

import com.pharmacie.config.ConnectionManager;
import com.pharmacie.dao.LotDao;
import com.pharmacie.dao.MedicamentDao;
import com.pharmacie.dao.MouvementStockDao;
import com.pharmacie.dao.VenteDao;
import com.pharmacie.model.*;
import com.pharmacie.util.AppException;
import com.pharmacie.util.Session;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service metier des ventes (sorties de stock).
 * L'enregistrement est TRANSACTIONNEL et garantit la coherence du stock :
 *  - verification de la disponibilite (quantites negatives interdites),
 *  - deduction des lots en FIFO (par date d'expiration),
 *  - journalisation des mouvements,
 *  - rollback complet si une etape echoue.
 */
public class VenteService {

    private final VenteDao venteDao = new VenteDao();
    private final LotDao lotDao = new LotDao();
    private final MedicamentDao medicamentDao = new MedicamentDao();
    private final MouvementStockDao mouvementDao = new MouvementStockDao();

    public List<Vente> lister() {
        return venteDao.findAll();
    }

    public List<Vente> rechercher(String terme) {
        if (terme == null || terme.isBlank()) return venteDao.findAll();
        return venteDao.search(terme.trim());
    }

    public List<Vente> parPeriode(LocalDate debut, LocalDate fin) {
        return venteDao.findByPeriode(debut, fin);
    }

    public Vente parId(int id) {
        return venteDao.findById(id);
    }

    public List<LigneVente> lignes(int venteId) {
        return venteDao.findLignes(venteId);
    }

    public String genererReference() {
        return venteDao.genererReference();
    }

    /**
     * Enregistre une vente de maniere transactionnelle.
     * @return la vente enregistree (avec id et reference)
     */
    public Vente enregistrer(Vente vente) {
        if (vente.getLignes() == null || vente.getLignes().isEmpty()) {
            throw new AppException("Le panier est vide. Ajoutez au moins un medicament.");
        }
        for (LigneVente l : vente.getLignes()) {
            if (l.getQuantite() <= 0) {
                throw new AppException("Les quantites vendues doivent etre superieures a zero.");
            }
            l.recalculer();
        }
        vente.calculerTotal();
        if (vente.getReference() == null || vente.getReference().isBlank()) {
            vente.setReference(venteDao.genererReference());
        }
        Integer userId = Session.getUtilisateur() != null ? Session.getUtilisateur().getId() : null;
        vente.setUtilisateurId(userId);

        Connection c = null;
        try {
            c = ConnectionManager.getConnection();
            c.setAutoCommit(false);

            // 1. Verification de la disponibilite (verrouillage des lignes medicament)
            for (LigneVente ligne : vente.getLignes()) {
                int dispo = medicamentDao.getStockPourUpdate(c, ligne.getMedicamentId());
                if (ligne.getQuantite() > dispo) {
                    throw new AppException("Stock insuffisant pour \"" + ligne.getMedicamentNom()
                            + "\" (disponible : " + dispo + ", demande : " + ligne.getQuantite() + ").");
                }
            }

            // 2. Enregistrement de l'entete
            venteDao.insertEntete(c, vente);

            // 3. Lignes + deduction FIFO des lots + mouvements
            for (LigneVente ligne : vente.getLignes()) {
                ligne.setVenteId(vente.getId());
                venteDao.insertLigne(c, ligne);

                deduireStockFifo(c, ligne, vente.getReference(), userId);

                // Stock agrege
                medicamentDao.ajusterStock(c, ligne.getMedicamentId(), -ligne.getQuantite());
            }

            c.commit();
            return vente;
        } catch (SQLException e) {
            rollback(c);
            throw new AppException("Echec de l'enregistrement de la vente : " + e.getMessage(), e);
        } catch (AppException e) {
            rollback(c);
            throw e;
        } finally {
            close(c);
        }
    }

    /** Deduit la quantite vendue des lots en FIFO et journalise un mouvement par lot. */
    private void deduireStockFifo(Connection c, LigneVente ligne, String refVente, Integer userId)
            throws SQLException {
        int restant = ligne.getQuantite();
        List<Lot> lots = lotDao.findLotsAvecStockPourVente(c, ligne.getMedicamentId());

        for (Lot lot : lots) {
            if (restant <= 0) break;
            int pris = Math.min(restant, lot.getQuantite());
            lotDao.retirerQuantite(c, lot.getId(), pris);
            restant -= pris;

            MouvementStock mv = new MouvementStock();
            mv.setMedicamentId(ligne.getMedicamentId());
            mv.setLotId(lot.getId());
            mv.setTypeMouvement(TypeMouvement.SORTIE);
            mv.setQuantite(pris);
            mv.setSource("Vente " + refVente);
            mv.setUtilisateurId(userId);
            mouvementDao.insert(c, mv);
        }

        if (restant > 0) {
            // Les lots ne couvrent pas tout : on journalise le reste sans lot
            // (cas limite si quantite_stock > somme des lots).
            MouvementStock mv = new MouvementStock();
            mv.setMedicamentId(ligne.getMedicamentId());
            mv.setLotId(null);
            mv.setTypeMouvement(TypeMouvement.SORTIE);
            mv.setQuantite(restant);
            mv.setSource("Vente " + refVente);
            mv.setUtilisateurId(userId);
            mouvementDao.insert(c, mv);
        }
    }

    private void rollback(Connection c) {
        if (c != null) {
            try { c.rollback(); } catch (SQLException ex) { /* ignore */ }
        }
    }

    private void close(Connection c) {
        if (c != null) {
            try { c.setAutoCommit(true); c.close(); } catch (SQLException ex) { /* ignore */ }
        }
    }
}
