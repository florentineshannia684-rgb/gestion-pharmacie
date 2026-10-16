package com.pharmacie.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Detail d'un medicament recu lors d'un approvisionnement.
 */
public class LigneApprovisionnement {

    private int id;
    private int approvisionnementId;
    private int medicamentId;
    private String medicamentNom;     // affichage
    private Integer lotId;
    private int quantite;
    private BigDecimal prixAchat = BigDecimal.ZERO;
    private String numeroLot;
    private LocalDate dateExpiration;

    public LigneApprovisionnement() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getApprovisionnementId() { return approvisionnementId; }
    public void setApprovisionnementId(int approvisionnementId) { this.approvisionnementId = approvisionnementId; }

    public int getMedicamentId() { return medicamentId; }
    public void setMedicamentId(int medicamentId) { this.medicamentId = medicamentId; }

    public String getMedicamentNom() { return medicamentNom; }
    public void setMedicamentNom(String medicamentNom) { this.medicamentNom = medicamentNom; }

    public Integer getLotId() { return lotId; }
    public void setLotId(Integer lotId) { this.lotId = lotId; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public BigDecimal getPrixAchat() { return prixAchat; }
    public void setPrixAchat(BigDecimal prixAchat) { this.prixAchat = prixAchat; }

    public String getNumeroLot() { return numeroLot; }
    public void setNumeroLot(String numeroLot) { this.numeroLot = numeroLot; }

    public LocalDate getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration = dateExpiration; }

    public BigDecimal getSousTotal() {
        return prixAchat.multiply(BigDecimal.valueOf(quantite));
    }
}
