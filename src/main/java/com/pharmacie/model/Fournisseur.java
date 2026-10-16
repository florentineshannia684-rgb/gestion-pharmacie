package com.pharmacie.model;

/**
 * Informations sur un fournisseur de medicaments.
 */
public class Fournisseur {

    private int id;
    private String nom;
    private String contact;
    private String telephone;
    private String email;
    private String adresse;

    public Fournisseur() {
    }

    public Fournisseur(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    @Override
    public String toString() {
        return nom;
    }
}
