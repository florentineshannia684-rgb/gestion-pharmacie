package com.pharmacie.model;

/**
 * Roles des utilisateurs de l'application et leurs droits d'acces.
 */
public enum Role {
    ADMIN("Administrateur"),
    PHARMACIEN("Pharmacien"),
    VENDEUR("Preparateur / Vendeur");

    private final String libelle;

    Role(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    /** Acces a la gestion des utilisateurs et des parametres. */
    public boolean peutGererUtilisateurs() {
        return this == ADMIN;
    }

    /** Acces a la gestion des medicaments, stocks, fournisseurs, appro. */
    public boolean peutGererStock() {
        return this == ADMIN || this == PHARMACIEN;
    }

    /** Acces a la caisse / ventes (tous les roles). */
    public boolean peutVendre() {
        return true;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
