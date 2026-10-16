package com.pharmacie.service;

import com.pharmacie.dao.UtilisateurDao;
import com.pharmacie.model.Role;
import com.pharmacie.model.Utilisateur;
import com.pharmacie.util.PasswordUtil;

/**
 * Prepare la base au premier demarrage :
 * garantit l'existence de comptes par defaut avec un mot de passe hache valide.
 *
 * Le script SQL insere des comptes avec des hash "placeholder" ; cette classe
 * remplace ces hash par de vrais hash BCrypt si necessaire, de sorte que la
 * connexion fonctionne toujours (identifiants documentes dans le README).
 */
public class DatabaseBootstrap {

    private final UtilisateurDao dao = new UtilisateurDao();

    /** Comptes par defaut : nom, mot de passe, nom complet, role. */
    private static final String[][] COMPTES_DEFAUT = {
            {"admin",   "admin123",   "Administrateur Systeme", "ADMIN"},
            {"pharma",  "pharma123",  "Pharmacien Principal",   "PHARMACIEN"},
            {"vendeur", "vendeur123", "Vendeur Comptoir",       "VENDEUR"},
    };

    /**
     * Verifie chaque compte par defaut :
     *  - s'il n'existe pas, le cree ;
     *  - si son hash est invalide (placeholder du SQL), le corrige.
     */
    public void initialiser() {
        for (String[] c : COMPTES_DEFAUT) {
            String login = c[0];
            String mdp = c[1];
            String nomComplet = c[2];
            Role role = Role.valueOf(c[3]);

            Utilisateur existant = dao.findByNomUtilisateur(login);
            if (existant == null) {
                Utilisateur u = new Utilisateur();
                u.setNomUtilisateur(login);
                u.setMotDePasse(PasswordUtil.hash(mdp));
                u.setNomComplet(nomComplet);
                u.setRole(role);
                u.setActif(true);
                dao.insert(u);
                System.out.println("[Bootstrap] Compte par defaut cree : " + login);
            } else if (!PasswordUtil.estHashValide(existant.getMotDePasse())
                    || !PasswordUtil.verifier(mdp, existant.getMotDePasse())) {
                // Hash placeholder ou invalide -> on definit un vrai hash
                dao.updateMotDePasse(existant.getId(), PasswordUtil.hash(mdp));
                System.out.println("[Bootstrap] Mot de passe par defaut initialise : " + login);
            }
        }
    }
}
