package com.pharmacie.dao;

import com.pharmacie.config.ConnectionManager;
import com.pharmacie.model.Fournisseur;
import com.pharmacie.util.AppException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acces aux donnees de la table fournisseur.
 */
public class FournisseurDao {

    private Fournisseur map(ResultSet rs) throws SQLException {
        Fournisseur f = new Fournisseur();
        f.setId(rs.getInt("id"));
        f.setNom(rs.getString("nom"));
        f.setContact(rs.getString("contact"));
        f.setTelephone(rs.getString("telephone"));
        f.setEmail(rs.getString("email"));
        f.setAdresse(rs.getString("adresse"));
        return f;
    }

    public List<Fournisseur> findAll() {
        String sql = "SELECT * FROM fournisseur ORDER BY nom";
        List<Fournisseur> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new AppException("Erreur lors du chargement des fournisseurs.", e);
        }
        return list;
    }

    public List<Fournisseur> search(String terme) {
        String sql = "SELECT * FROM fournisseur WHERE nom LIKE ? OR contact LIKE ? OR telephone LIKE ? ORDER BY nom";
        List<Fournisseur> list = new ArrayList<>();
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
            throw new AppException("Erreur lors de la recherche de fournisseurs.", e);
        }
        return list;
    }

    public Fournisseur findById(int id) {
        String sql = "SELECT * FROM fournisseur WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors de la lecture du fournisseur.", e);
        }
    }

    public int count() {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM fournisseur");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new AppException("Erreur lors du comptage des fournisseurs.", e);
        }
    }

    public int insert(Fournisseur f) {
        String sql = "INSERT INTO fournisseur (nom, contact, telephone, email, adresse) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, f.getNom());
            ps.setString(2, f.getContact());
            ps.setString(3, f.getTelephone());
            ps.setString(4, f.getEmail());
            ps.setString(5, f.getAdresse());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) f.setId(keys.getInt(1));
            }
            return f.getId();
        } catch (SQLException e) {
            throw new AppException("Erreur lors de la creation du fournisseur.", e);
        }
    }

    public void update(Fournisseur f) {
        String sql = "UPDATE fournisseur SET nom=?, contact=?, telephone=?, email=?, adresse=? WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, f.getNom());
            ps.setString(2, f.getContact());
            ps.setString(3, f.getTelephone());
            ps.setString(4, f.getEmail());
            ps.setString(5, f.getAdresse());
            ps.setInt(6, f.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Erreur lors de la mise a jour du fournisseur.", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM fournisseur WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Impossible de supprimer ce fournisseur (approvisionnements lies).", e);
        }
    }
}
