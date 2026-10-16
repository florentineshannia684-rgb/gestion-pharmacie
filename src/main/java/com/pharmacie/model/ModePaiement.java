package com.pharmacie.model;

/**
 * Modes de paiement acceptes lors d'une vente.
 */
public enum ModePaiement {
    ESPECES("Especes"),
    CARTE("Carte bancaire"),
    MOBILE("Mobile Money"),
    AUTRE("Autre");

    private final String libelle;

    ModePaiement(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
