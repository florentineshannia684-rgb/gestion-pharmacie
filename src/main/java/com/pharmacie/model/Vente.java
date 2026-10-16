package com.pharmacie.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entete d'une vente (ticket de caisse).
 */
public class Vente {

    private int id;
    private String reference;
    private Integer clientId;
    private String clientNom;         // affichage / recu
    private Integer utilisateurId;
    private String utilisateurNom;    // affichage / recu
    private LocalDateTime dateVente;
    private BigDecimal montantTotal = BigDecimal.ZERO;
    private ModePaiement modePaiement = ModePaiement.ESPECES;
    private List<LigneVente> lignes = new ArrayList<>();

    public Vente() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    public String getClientNom() { return clientNom; }
    public void setClientNom(String clientNom) { this.clientNom = clientNom; }

    public Integer getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Integer utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getUtilisateurNom() { return utilisateurNom; }
    public void setUtilisateurNom(String utilisateurNom) { this.utilisateurNom = utilisateurNom; }

    public LocalDateTime getDateVente() { return dateVente; }
    public void setDateVente(LocalDateTime dateVente) { this.dateVente = dateVente; }

    public BigDecimal getMontantTotal() { return montantTotal; }
    public void setMontantTotal(BigDecimal montantTotal) { this.montantTotal = montantTotal; }

    public ModePaiement getModePaiement() { return modePaiement; }
    public void setModePaiement(ModePaiement modePaiement) { this.modePaiement = modePaiement; }

    public List<LigneVente> getLignes() { return lignes; }
    public void setLignes(List<LigneVente> lignes) { this.lignes = lignes; }

    public void ajouterLigne(LigneVente ligne) {
        this.lignes.add(ligne);
    }

    /** Recalcule le montant total a partir des lignes. */
    public BigDecimal calculerTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (LigneVente l : lignes) {
            total = total.add(l.getSousTotal());
        }
        this.montantTotal = total;
        return total;
    }

    public int getNombreArticles() {
        return lignes.stream().mapToInt(LigneVente::getQuantite).sum();
    }
}
