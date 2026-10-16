package com.pharmacie.service;

import com.pharmacie.dao.CategorieDao;
import com.pharmacie.model.Categorie;
import com.pharmacie.util.AppException;

import java.util.List;

/**
 * Service metier des categories.
 */
public class CategorieService {

    private final CategorieDao dao = new CategorieDao();

    public List<Categorie> lister() {
        return dao.findAll();
    }

    public void enregistrer(Categorie cat) {
        if (cat.getNom() == null || cat.getNom().isBlank()) {
            throw new AppException("Le nom de la categorie est obligatoire.");
        }
        if (cat.getId() == 0) {
            dao.insert(cat);
        } else {
            dao.update(cat);
        }
    }

    public void supprimer(int id) {
        dao.delete(id);
    }
}
