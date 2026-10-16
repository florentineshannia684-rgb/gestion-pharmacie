package com.pharmacie.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Informations generales sur un medicament.
 * Le champ quantiteStock est l'agregat du stock (somme des lots),
 * tenu a jour de maniere transactionnelle lors des appro et ventes.
 */
public class Medicament {

    private int id;
    private String reference;
    private String nom;
    private String forme;
    private String dosage;
    private BigDecimal prixVente = BigDecimal.ZERO;
    private int quantiteStock;
    private int seuilStock = 10;
    private Integer categorieId;      // peut etre null
    private String categorieNom;      // champ de jointure (lecture seule)
    private String imagePath;
    private LocalDateTime dateCreation;

    public Medicament() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getForme() { return forme; }
    public void setForme(String forme) { this.forme = forme; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public BigDecimal getPrixVente() { return prixVente; }
    public void setPrixVente(BigDecimal prixVente) { this.prixVente = prixVente; }

    public int getQuantiteStock() { return quantiteStock; }
    public void setQuantiteStock(int quantiteStock) { this.quantiteStock = quantiteStock; }

    public int getSeuilStock() { return seuilStock; }
    public void setSeuilStock(int seuilStock) { this.seuilStock = seuilStock; }

    public Integer getCategorieId() { return categorieId; }
    public void setCategorieId(Integer categorieId) { this.categorieId = categorieId; }

    public String getCategorieNom() { return categorieNom; }
    public void setCategorieNom(String categorieNom) { this.categorieNom = categorieNom; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    /** Indique si le stock est au niveau ou en dessous du seuil defini. */
    public boolean isStockFaible() {
        return quantiteStock <= seuilStock;
    }

    @Override
    public String toString() {
        return nom + (dosage != null && !dosage.isBlank() ? " " + dosage : "")
                + " [" + reference + "]";
    }
}
