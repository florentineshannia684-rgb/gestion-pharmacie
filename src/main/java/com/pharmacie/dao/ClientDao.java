package com.pharmacie.dao;

import com.pharmacie.config.ConnectionManager;
import com.pharmacie.model.Client;
import com.pharmacie.util.AppException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acces aux donnees de la table client.
 */
public class ClientDao {

    private Client map(ResultSet rs) throws SQLException {
        Client cl = new Client();
        cl.setId(rs.getInt("id"));
        cl.setNom(rs.getString("nom"));
        cl.setPrenom(rs.getString("prenom"));
        cl.setTelephone(rs.getString("telephone"));
        cl.setEmail(rs.getString("email"));
        cl.setAdresse(rs.getString("adresse"));
        return cl;
    }

    public List<Client> findAll() {
        String sql = "SELECT * FROM client ORDER BY nom, prenom";
        List<Client> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new AppException("Erreur lors du chargement des clients.", e);
        }
        return list;
    }

    public List<Client> search(String terme) {
        String sql = "SELECT * FROM client WHERE nom LIKE ? OR prenom LIKE ? OR telephone LIKE ? ORDER BY nom, prenom";
        List<Client> list = new ArrayList<>();
        String like = "%" + terme + "%";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors de la recherche de clients.", e);
        }
        return list;
    }

    public Client findById(int id) {
        String sql = "SELECT * FROM client WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors de la lecture du client.", e);
        }
    }

    public int count() {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM client");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new AppException("Erreur lors du comptage des clients.", e);
        }
    }

    public int insert(Client cl) {
        String sql = "INSERT INTO client (nom, prenom, telephone, email, adresse) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cl.getNom());
            ps.setString(2, cl.getPrenom());
            ps.setString(3, cl.getTelephone());
            ps.setString(4, cl.getEmail());
            ps.setString(5, cl.getAdresse());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) cl.setId(keys.getInt(1));
            }
            return cl.getId();
        } catch (SQLException e) {
            throw new AppException("Erreur lors de la creation du client.", e);
        }
    }

    public void update(Client cl) {
        String sql = "UPDATE client SET nom=?, prenom=?, telephone=?, email=?, adresse=? WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cl.getNom());
            ps.setString(2, cl.getPrenom());
            ps.setString(3, cl.getTelephone());
            ps.setString(4, cl.getEmail());
            ps.setString(5, cl.getAdresse());
            ps.setInt(6, cl.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Erreur lors de la mise a jour du client.", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM client WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Impossible de supprimer ce client (ventes liees).", e);
        }
    }
}
