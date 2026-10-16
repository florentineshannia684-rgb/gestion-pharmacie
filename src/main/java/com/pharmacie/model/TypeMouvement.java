package com.pharmacie.model;

/**
 * Type d'un mouvement de stock.
 */
public enum TypeMouvement {
    ENTREE("Entree"),
    SORTIE("Sortie"),
    AJUSTEMENT("Ajustement");

    private final String libelle;

    TypeMouvement(String libelle) {
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
