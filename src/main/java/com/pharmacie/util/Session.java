package com.pharmacie.util;

import com.pharmacie.model.Role;
import com.pharmacie.model.Utilisateur;

/**
 * Conserve l'utilisateur actuellement connecte (session applicative).
 */
public final class Session {

    private static Utilisateur utilisateurConnecte;

    private Session() {
    }

    public static void ouvrir(Utilisateur u) {
        utilisateurConnecte = u;
    }

    public static void fermer() {
        utilisateurConnecte = null;
    }

    public static Utilisateur getUtilisateur() {
        return utilisateurConnecte;
    }

    public static boolean estConnecte() {
        return utilisateurConnecte != null;
    }

    public static Role getRole() {
        return utilisateurConnecte == null ? null : utilisateurConnecte.getRole();
    }

    public static boolean peutGererUtilisateurs() {
        return estConnecte() && utilisateurConnecte.getRole().peutGererUtilisateurs();
    }

    public static boolean peutGererStock() {
        return estConnecte() && utilisateurConnecte.getRole().peutGererStock();
    }
}
