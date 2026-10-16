package com.pharmacie.service;

import com.pharmacie.config.ConnectionManager;
import com.pharmacie.dao.ApprovisionnementDao;
import com.pharmacie.dao.LotDao;
import com.pharmacie.dao.MedicamentDao;
import com.pharmacie.dao.MouvementStockDao;
import com.pharmacie.model.*;
import com.pharmacie.util.AppException;
import com.pharmacie.util.Session;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Service metier des approvisionnements (entrees de stock).
 * L'enregistrement est TRANSACTIONNEL : entete + lignes + lots + stock + mouvements
 * sont valides ensemble, ou annules en cas d'erreur.
 */
public class ApprovisionnementService {

    private final ApprovisionnementDao approDao = new ApprovisionnementDao();
    private final LotDao lotDao = new LotDao();
    private final MedicamentDao medicamentDao = new MedicamentDao();
    private final MouvementStockDao mouvementDao = new MouvementStockDao();

    public List<Approvisionnement> lister() {
        return approDao.findAll();
    }

    public List<LigneApprovisionnement> lignes(int approId) {
        return approDao.findLignes(approId);
    }

    public String genererReference() {
        return approDao.genererReference();
    }

    /**
     * Enregistre un approvisionnement complet de maniere transactionnelle.
     * @return la reference de l'approvisionnement cree
     */
    public String enregistrer(Approvisionnement appro) {
        if (appro.getLignes() == null || appro.getLignes().isEmpty()) {
            throw new AppException("Ajoutez au moins une ligne a l'approvisionnement.");
        }
        for (LigneApprovisionnement l : appro.getLignes()) {
            if (l.getQuantite() <= 0) {
                throw new AppException("Les quantites recues doivent etre superieures a zero.");
            }
        }
        appro.calculerTotal();
        if (appro.getReference() == null || appro.getReference().isBlank()) {
            appro.setReference(approDao.genererReference());
        }
        Integer userId = Session.getUtilisateur() != null ? Session.getUtilisateur().getId() : null;
        appro.setUtilisateurId(userId);

        Connection c = null;
        try {
            c = ConnectionManager.getConnection();
            c.setAutoCommit(false);

            approDao.insertEntete(c, appro);

            for (LigneApprovisionnement ligne : appro.getLignes()) {
                int medId = ligne.getMedicamentId();
                String numLot = (ligne.getNumeroLot() == null || ligne.getNumeroLot().isBlank())
                        ? "LOT-" + appro.getReference() + "-" + medId
                        : ligne.getNumeroLot().trim();
                ligne.setNumeroLot(numLot);

                // Lot existant (meme medicament + meme numero) ? sinon creation.
                Integer lotId = lotDao.findLotId(c, medId, numLot);
                if (lotId == null) {
                    Lot lot = new Lot();
                    lot.setMedicamentId(medId);
                    lot.setNumeroLot(numLot);
                    lot.setQuantite(ligne.getQuantite());
                    lot.setPrixAchat(ligne.getPrixAchat());
                    lot.setDateExpiration(ligne.getDateExpiration());
                    lotId = lotDao.insert(c, lot);
                } else {
                    lotDao.ajouterQuantite(c, lotId, ligne.getQuantite());
                }
                ligne.setLotId(lotId);
                ligne.setApprovisionnementId(appro.getId());

                approDao.insertLigne(c, ligne);

                // Augmente le stock agrege du medicament
                medicamentDao.ajusterStock(c, medId, ligne.getQuantite());

                // Journalise le mouvement d'entree
                MouvementStock mv = new MouvementStock();
                mv.setMedicamentId(medId);
                mv.setLotId(lotId);
                mv.setTypeMouvement(TypeMouvement.ENTREE);
                mv.setQuantite(ligne.getQuantite());
                mv.setSource("Appro " + appro.getReference());
                mv.setUtilisateurId(userId);
                mouvementDao.insert(c, mv);
            }

            c.commit();
            return appro.getReference();
        } catch (SQLException e) {
            rollback(c);
            throw new AppException("Echec de l'enregistrement de l'approvisionnement : " + e.getMessage(), e);
        } catch (AppException e) {
            rollback(c);
            throw e;
        } finally {
            close(c);
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
