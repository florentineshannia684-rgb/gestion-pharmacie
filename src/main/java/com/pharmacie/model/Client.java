package com.pharmacie.model;

/**
 * Informations utiles d'un client pour identifier une vente et son recu.
 * Le client n'utilise pas directement l'application.
 */
public class Client {

    private int id;
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private String adresse;

    public Client() {
    }

    public Client(int id, String nom, String prenom) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getNomComplet() {
        return (prenom == null ? "" : prenom + " ") + (nom == null ? "" : nom);
    }

    @Override
    public String toString() {
        return getNomComplet().trim();
    }
}
