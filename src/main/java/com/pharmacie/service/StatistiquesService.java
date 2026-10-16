package com.pharmacie.service;

import com.pharmacie.dao.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service d'agregation des statistiques pour le tableau de bord et les rapports.
 */
public class StatistiquesService {

    private final MedicamentDao medicamentDao = new MedicamentDao();
    private final VenteDao venteDao = new VenteDao();
    private final ClientDao clientDao = new ClientDao();
    private final FournisseurDao fournisseurDao = new FournisseurDao();
    private final LotDao lotDao = new LotDao();

    public int nbMedicaments() {
        return medicamentDao.count();
    }

    public int nbStockFaible() {
        return medicamentDao.countStockFaible();
    }

    public int nbProchesExpiration() {
        return lotDao.countProchesExpiration(StockService.SEUIL_EXPIRATION_JOURS);
    }

    public int nbClients() {
        return clientDao.count();
    }

    public int nbFournisseurs() {
        return fournisseurDao.count();
    }

    public int nbVentesDuJour() {
        LocalDate t = LocalDate.now();
        return venteDao.countVentes(t, t);
    }

    public BigDecimal chiffreAffairesDuJour() {
        LocalDate t = LocalDate.now();
        return venteDao.chiffreAffaires(t, t);
    }

    public int nbVentes(LocalDate debut, LocalDate fin) {
        return venteDao.countVentes(debut, fin);
    }

    public BigDecimal chiffreAffaires(LocalDate debut, LocalDate fin) {
        return venteDao.chiffreAffaires(debut, fin);
    }

    public List<Object[]> topMedicaments(LocalDate debut, LocalDate fin, int limite) {
        return venteDao.topMedicaments(debut, fin, limite);
    }

    public List<Object[]> chiffreAffairesParJour(LocalDate debut, LocalDate fin) {
        return venteDao.chiffreAffairesParJour(debut, fin);
    }
}
