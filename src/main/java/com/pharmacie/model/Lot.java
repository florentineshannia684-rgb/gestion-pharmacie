package com.pharmacie.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lot d'un medicament : numero de lot, quantite et date d'expiration.
 */
public class Lot {

    private int id;
    private int medicamentId;
    private String medicamentNom;     // champ de jointure (lecture seule)
    private String numeroLot;
    private int quantite;
    private BigDecimal prixAchat = BigDecimal.ZERO;
    private LocalDate dateExpiration;
    private LocalDateTime dateReception;

    public Lot() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMedicamentId() { return medicamentId; }
    public void setMedicamentId(int medicamentId) { this.medicamentId = medicamentId; }

    public String getMedicamentNom() { return medicamentNom; }
    public void setMedicamentNom(String medicamentNom) { this.medicamentNom = medicamentNom; }

    public String getNumeroLot() { return numeroLot; }
    public void setNumeroLot(String numeroLot) { this.numeroLot = numeroLot; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public BigDecimal getPrixAchat() { return prixAchat; }
    public void setPrixAchat(BigDecimal prixAchat) { this.prixAchat = prixAchat; }

    public LocalDate getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration = dateExpiration; }

    public LocalDateTime getDateReception() { return dateReception; }
    public void setDateReception(LocalDateTime dateReception) { this.dateReception = dateReception; }

    /** Nombre de jours avant expiration (negatif si deja expire). */
    public long joursAvantExpiration() {
        if (dateExpiration == null) return Long.MAX_VALUE;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dateExpiration);
    }

    public boolean isExpire() {
        return dateExpiration != null && dateExpiration.isBefore(LocalDate.now());
    }

    public boolean isProcheExpiration(int seuilJours) {
        long j = joursAvantExpiration();
        return j >= 0 && j <= seuilJours;
    }

    @Override
    public String toString() {
        return numeroLot + " (qte " + quantite + ")";
    }
}
