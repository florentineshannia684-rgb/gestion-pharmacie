package com.pharmacie.model;

import java.time.LocalDateTime;

/**
 * Compte d'un membre du personnel de la pharmacie.
 */
public class Utilisateur {

    private int id;
    private String nomUtilisateur;
    private String motDePasse;      // hash BCrypt
    private String nomComplet;
    private Role role;
    private boolean actif;
    private LocalDateTime dateCreation;

    public Utilisateur() {
        this.actif = true;
        this.role = Role.VENDEUR;
    }

    public Utilisateur(int id, String nomUtilisateur, String nomComplet, Role role, boolean actif) {
        this.id = id;
        this.nomUtilisateur = nomUtilisateur;
        this.nomComplet = nomComplet;
        this.role = role;
        this.actif = actif;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomUtilisateur() { return nomUtilisateur; }
    public void setNomUtilisateur(String nomUtilisateur) { this.nomUtilisateur = nomUtilisateur; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    @Override
    public String toString() {
        return nomComplet + " (" + nomUtilisateur + ")";
    }
}
