package com.pharmacie.service;

import com.pharmacie.dao.UtilisateurDao;
import com.pharmacie.model.Utilisateur;
import com.pharmacie.util.AppException;
import com.pharmacie.util.PasswordUtil;

import java.util.List;

/**
 * Service metier de gestion des comptes utilisateurs.
 */
public class UtilisateurService {

    private final UtilisateurDao dao = new UtilisateurDao();

    public List<Utilisateur> lister() {
        return dao.findAll();
    }

    /**
     * Cree un utilisateur. Le mot de passe en clair est hache ici.
     */
    public void creer(Utilisateur u, String motDePasseClair) {
        validerBase(u);
        if (motDePasseClair == null || motDePasseClair.length() < 4) {
            throw new AppException("Le mot de passe doit contenir au moins 4 caracteres.");
        }
        u.setMotDePasse(PasswordUtil.hash(motDePasseClair));
        dao.insert(u);
    }

    public void modifier(Utilisateur u) {
        validerBase(u);
        dao.update(u);
    }

    public void changerMotDePasse(int id, String nouveauMdp) {
        if (nouveauMdp == null || nouveauMdp.length() < 4) {
            throw new AppException("Le mot de passe doit contenir au moins 4 caracteres.");
        }
        dao.updateMotDePasse(id, PasswordUtil.hash(nouveauMdp));
    }

    public void supprimer(int id) {
        dao.delete(id);
    }

    private void validerBase(Utilisateur u) {
        if (u.getNomUtilisateur() == null || u.getNomUtilisateur().isBlank()) {
            throw new AppException("L'identifiant est obligatoire.");
        }
        if (u.getNomComplet() == null || u.getNomComplet().isBlank()) {
            throw new AppException("Le nom complet est obligatoire.");
        }
        if (u.getRole() == null) {
            throw new AppException("Le role est obligatoire.");
        }
    }
}
