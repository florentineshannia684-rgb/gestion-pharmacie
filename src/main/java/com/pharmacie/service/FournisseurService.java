package com.pharmacie.service;

import com.pharmacie.dao.ApprovisionnementDao;
import com.pharmacie.dao.FournisseurDao;
import com.pharmacie.model.Approvisionnement;
import com.pharmacie.model.Fournisseur;
import com.pharmacie.util.AppException;

import java.util.List;

/**
 * Service metier des fournisseurs.
 */
public class FournisseurService {

    private final FournisseurDao dao = new FournisseurDao();
    private final ApprovisionnementDao approDao = new ApprovisionnementDao();

    public List<Fournisseur> lister() {
        return dao.findAll();
    }

    public List<Fournisseur> rechercher(String terme) {
        if (terme == null || terme.isBlank()) return dao.findAll();
        return dao.search(terme.trim());
    }

    public List<Approvisionnement> approvisionnements(int fournisseurId) {
        return approDao.findByFournisseur(fournisseurId);
    }

    public void enregistrer(Fournisseur f) {
        if (f.getNom() == null || f.getNom().isBlank()) {
            throw new AppException("Le nom du fournisseur est obligatoire.");
        }
        if (f.getId() == 0) dao.insert(f);
        else dao.update(f);
    }

    public void supprimer(int id) {
        dao.delete(id);
    }
}
