package com.pharmacie.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entete d'un approvisionnement (reception d'une livraison).
 */
public class Approvisionnement {

    private int id;
    private String reference;
    private Integer fournisseurId;
    private String fournisseurNom;    // affichage
    private Integer utilisateurId;
    private String utilisateurNom;    // affichage
    private LocalDateTime dateAppro;
    private BigDecimal montantTotal = BigDecimal.ZERO;
    private List<LigneApprovisionnement> lignes = new ArrayList<>();

    public Approvisionnement() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Integer getFournisseurId() { return fournisseurId; }
    public void setFournisseurId(Integer fournisseurId) { this.fournisseurId = fournisseurId; }

    public String getFournisseurNom() { return fournisseurNom; }
    public void setFournisseurNom(String fournisseurNom) { this.fournisseurNom = fournisseurNom; }

    public Integer getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Integer utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getUtilisateurNom() { return utilisateurNom; }
    public void setUtilisateurNom(String utilisateurNom) { this.utilisateurNom = utilisateurNom; }

    public LocalDateTime getDateAppro() { return dateAppro; }
    public void setDateAppro(LocalDateTime dateAppro) { this.dateAppro = dateAppro; }

    public BigDecimal getMontantTotal() { return montantTotal; }
    public void setMontantTotal(BigDecimal montantTotal) { this.montantTotal = montantTotal; }

    public List<LigneApprovisionnement> getLignes() { return lignes; }
    public void setLignes(List<LigneApprovisionnement> lignes) { this.lignes = lignes; }

    public void ajouterLigne(LigneApprovisionnement ligne) {
        this.lignes.add(ligne);
    }

    /** Recalcule le montant total a partir des lignes. */
    public BigDecimal calculerTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (LigneApprovisionnement l : lignes) {
            total = total.add(l.getSousTotal());
        }
        this.montantTotal = total;
        return total;
    }
}
