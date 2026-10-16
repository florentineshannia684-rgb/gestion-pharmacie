package com.pharmacie.model;

import java.time.LocalDateTime;

/**
 * Historique d'une entree ou sortie de stock.
 */
public class MouvementStock {

    private int id;
    private int medicamentId;
    private String medicamentNom;     // affichage
    private Integer lotId;
    private String numeroLot;         // affichage
    private TypeMouvement typeMouvement;
    private int quantite;
    private String source;
    private Integer utilisateurId;
    private String utilisateurNom;    // affichage
    private LocalDateTime dateMouvement;

    public MouvementStock() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMedicamentId() { return medicamentId; }
    public void setMedicamentId(int medicamentId) { this.medicamentId = medicamentId; }

    public String getMedicamentNom() { return medicamentNom; }
    public void setMedicamentNom(String medicamentNom) { this.medicamentNom = medicamentNom; }

    public Integer getLotId() { return lotId; }
    public void setLotId(Integer lotId) { this.lotId = lotId; }

    public String getNumeroLot() { return numeroLot; }
    public void setNumeroLot(String numeroLot) { this.numeroLot = numeroLot; }

    public TypeMouvement getTypeMouvement() { return typeMouvement; }
    public void setTypeMouvement(TypeMouvement typeMouvement) { this.typeMouvement = typeMouvement; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Integer getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Integer utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getUtilisateurNom() { return utilisateurNom; }
    public void setUtilisateurNom(String utilisateurNom) { this.utilisateurNom = utilisateurNom; }

    public LocalDateTime getDateMouvement() { return dateMouvement; }
    public void setDateMouvement(LocalDateTime dateMouvement) { this.dateMouvement = dateMouvement; }
}
