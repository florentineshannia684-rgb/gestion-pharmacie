package com.pharmacie.service;

import com.pharmacie.dao.ClientDao;
import com.pharmacie.dao.VenteDao;
import com.pharmacie.model.Client;
import com.pharmacie.model.Vente;
import com.pharmacie.util.AppException;

import java.util.List;

/**
 * Service metier des clients.
 */
public class ClientService {

    private final ClientDao dao = new ClientDao();
    private final VenteDao venteDao = new VenteDao();

    public List<Client> lister() {
        return dao.findAll();
    }

    public List<Client> rechercher(String terme) {
        if (terme == null || terme.isBlank()) return dao.findAll();
        return dao.search(terme.trim());
    }

    public List<Vente> historiqueVentes(int clientId) {
        return venteDao.findByClient(clientId);
    }

    public void enregistrer(Client cl) {
        if (cl.getNom() == null || cl.getNom().isBlank()) {
            throw new AppException("Le nom du client est obligatoire.");
        }
        if (cl.getId() == 0) dao.insert(cl);
        else dao.update(cl);
    }

    public void supprimer(int id) {
        dao.delete(id);
    }
}
