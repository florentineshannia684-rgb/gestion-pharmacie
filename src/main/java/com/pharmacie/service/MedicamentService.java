package com.pharmacie.service;

import com.pharmacie.dao.MedicamentDao;
import com.pharmacie.model.Medicament;
import com.pharmacie.util.AppException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service metier des medicaments.
 */
public class MedicamentService {

    private final MedicamentDao dao = new MedicamentDao();

    public List<Medicament> lister() {
        return dao.findAll();
    }

    public List<Medicament> rechercher(String terme, Integer categorieId) {
        return dao.search(terme, categorieId);
    }

    public Medicament parId(int id) {
        return dao.findById(id);
    }

    public String genererReference() {
        return dao.genererReference();
    }

    public void enregistrer(Medicament m) {
        valider(m);
        if (m.getId() == 0) dao.insert(m);
        else dao.update(m);
    }

    private void valider(Medicament m) {
        if (m.getReference() == null || m.getReference().isBlank()) {
            throw new AppException("La reference est obligatoire.");
        }
        if (m.getNom() == null || m.getNom().isBlank()) {
            throw new AppException("Le nom du medicament est obligatoire.");
        }
        if (m.getPrixVente() == null || m.getPrixVente().compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException("Le prix de vente doit etre positif ou nul.");
        }
        if (m.getSeuilStock() < 0) {
            throw new AppException("Le seuil de stock ne peut pas etre negatif.");
        }
    }

    public void supprimer(int id) {
        dao.delete(id);
    }
}
