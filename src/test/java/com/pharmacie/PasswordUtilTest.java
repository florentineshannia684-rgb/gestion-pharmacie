package com.pharmacie;

import com.pharmacie.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests du hachage des mots de passe (ne necessitent pas de base de donnees).
 */
class PasswordUtilTest {

    @Test
    void hashEtVerificationReussissent() {
        String hash = PasswordUtil.hash("secret123");
        assertNotNull(hash);
        assertTrue(PasswordUtil.estHashValide(hash));
        assertTrue(PasswordUtil.verifier("secret123", hash));
    }

    @Test
    void mauvaisMotDePasseEchoue() {
        String hash = PasswordUtil.hash("secret123");
        assertFalse(PasswordUtil.verifier("mauvais", hash));
    }

    @Test
    void hashInvalideEstDetecte() {
        assertFalse(PasswordUtil.estHashValide("pas-un-hash"));
        assertFalse(PasswordUtil.verifier("x", "pas-un-hash"));
        assertFalse(PasswordUtil.verifier("x", null));
    }
}
