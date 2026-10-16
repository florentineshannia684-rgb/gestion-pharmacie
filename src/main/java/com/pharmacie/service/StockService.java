package com.pharmacie.service;

import com.pharmacie.dao.LotDao;
import com.pharmacie.dao.MedicamentDao;
import com.pharmacie.dao.MouvementStockDao;
import com.pharmacie.model.Lot;
import com.pharmacie.model.Medicament;
import com.pharmacie.model.MouvementStock;

import java.util.List;

/**
 * Service metier du suivi des stocks, lots et mouvements.
 */
public class StockService {

    /** Seuil (en jours) pour considerer un lot comme proche de l'expiration. */
    public static final int SEUIL_EXPIRATION_JOURS = 60;

    private final MedicamentDao medicamentDao = new MedicamentDao();
    private final LotDao lotDao = new LotDao();
    private final MouvementStockDao mouvementDao = new MouvementStockDao();

    public List<Medicament> stockActuel() {
        return medicamentDao.findAll();
    }

    public List<Medicament> stockFaible() {
        return medicamentDao.findStockFaible();
    }

    public List<Lot> tousLesLots() {
        return lotDao.findAll();
    }

    public List<Lot> lotsParMedicament(int medicamentId) {
        return lotDao.findByMedicament(medicamentId);
    }

    public List<Lot> lotsProchesExpiration() {
        return lotDao.findProchesExpiration(SEUIL_EXPIRATION_JOURS);
    }

    public List<MouvementStock> mouvementsRecents(int limite) {
        return mouvementDao.findRecents(limite);
    }

    public List<MouvementStock> mouvementsParMedicament(int medicamentId) {
        return mouvementDao.findByMedicament(medicamentId);
    }

    public int nbStockFaible() {
        return medicamentDao.countStockFaible();
    }

    public int nbProchesExpiration() {
        return lotDao.countProchesExpiration(SEUIL_EXPIRATION_JOURS);
    }
}
