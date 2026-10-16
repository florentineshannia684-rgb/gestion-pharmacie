package com.pharmacie.service;

import com.pharmacie.dao.UtilisateurDao;
import com.pharmacie.model.Utilisateur;
import com.pharmacie.util.AppException;
import com.pharmacie.util.PasswordUtil;
import com.pharmacie.util.Session;

/**
 * Service d'authentification.
 */
public class AuthService {

    private final UtilisateurDao dao = new UtilisateurDao();

    /**
     * Authentifie un utilisateur et ouvre la session.
     * @throws AppException si l'authentification echoue
     */
    public Utilisateur connexion(String nomUtilisateur, String motDePasse) {
        if (nomUtilisateur == null || nomUtilisateur.isBlank()
                || motDePasse == null || motDePasse.isBlank()) {
            throw new AppException("Veuillez saisir l'identifiant et le mot de passe.");
        }
        Utilisateur u = dao.findByNomUtilisateur(nomUtilisateur.trim());
        if (u == null || !PasswordUtil.verifier(motDePasse, u.getMotDePasse())) {
            throw new AppException("Identifiant ou mot de passe incorrect.");
        }
        if (!u.isActif()) {
            throw new AppException("Ce compte est desactive. Contactez un administrateur.");
        }
        Session.ouvrir(u);
        return u;
    }

    public void deconnexion() {
        Session.fermer();
    }
}
