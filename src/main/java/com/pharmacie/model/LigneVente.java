package com.pharmacie.model;

import java.math.BigDecimal;

/**
 * Ligne d'une vente : medicament, quantite et prix.
 */
public class LigneVente {

    private int id;
    private int venteId;
    private int medicamentId;
    private String medicamentNom;     // affichage / recu
    private String reference;         // affichage / recu
    private int quantite;
    private BigDecimal prixUnitaire = BigDecimal.ZERO;
    private BigDecimal sousTotal = BigDecimal.ZERO;

    public LigneVente() {
    }

    public LigneVente(Medicament m, int quantite) {
        this.medicamentId = m.getId();
        this.medicamentNom = m.getNom();
        this.reference = m.getReference();
        this.prixUnitaire = m.getPrixVente();
        this.quantite = quantite;
        recalculer();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getVenteId() { return venteId; }
    public void setVenteId(int venteId) { this.venteId = venteId; }

    public int getMedicamentId() { return medicamentId; }
    public void setMedicamentId(int medicamentId) { this.medicamentId = medicamentId; }

    public String getMedicamentNom() { return medicamentNom; }
    public void setMedicamentNom(String medicamentNom) { this.medicamentNom = medicamentNom; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; recalculer(); }

    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; recalculer(); }

    public BigDecimal getSousTotal() { return sousTotal; }
    public void setSousTotal(BigDecimal sousTotal) { this.sousTotal = sousTotal; }

    public final void recalculer() {
        if (prixUnitaire != null) {
            this.sousTotal = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        }
    }
}
