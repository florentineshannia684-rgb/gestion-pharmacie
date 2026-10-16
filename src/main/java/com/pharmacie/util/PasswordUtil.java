package com.pharmacie.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Hachage et verification des mots de passe avec BCrypt.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    /** Hache un mot de passe en clair. */
    public static String hash(String motDePasseClair) {
        return BCrypt.hashpw(motDePasseClair, BCrypt.gensalt(10));
    }

    /** Verifie qu'un mot de passe en clair correspond a un hash BCrypt. */
    public static boolean verifier(String motDePasseClair, String hash) {
        if (motDePasseClair == null || hash == null || hash.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(motDePasseClair, hash);
        } catch (IllegalArgumentException e) {
            // Hash mal forme (ex. donnee seed invalide)
            return false;
        }
    }

    /** Indique si une chaine ressemble a un hash BCrypt valide. */
    public static boolean estHashValide(String hash) {
        return hash != null && hash.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }
}
