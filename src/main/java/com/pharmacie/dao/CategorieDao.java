package com.pharmacie.dao;

import com.pharmacie.config.ConnectionManager;
import com.pharmacie.model.Categorie;
import com.pharmacie.util.AppException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acces aux donnees de la table categorie.
 */
public class CategorieDao {

    private Categorie map(ResultSet rs) throws SQLException {
        return new Categorie(rs.getInt("id"), rs.getString("nom"), rs.getString("description"));
    }

    public List<Categorie> findAll() {
        String sql = "SELECT * FROM categorie ORDER BY nom";
        List<Categorie> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new AppException("Erreur lors du chargement des categories.", e);
        }
        return list;
    }

    public Categorie findById(int id) {
        String sql = "SELECT * FROM categorie WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors de la lecture de la categorie.", e);
        }
    }

    public int count() {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM categorie");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new AppException("Erreur lors du comptage des categories.", e);
        }
    }

    public int insert(Categorie cat) {
        String sql = "INSERT INTO categorie (nom, description) VALUES (?, ?)";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cat.getNom());
            ps.setString(2, cat.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) cat.setId(keys.getInt(1));
            }
            return cat.getId();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AppException("Cette categorie existe deja.", e);
        } catch (SQLException e) {
            throw new AppException("Erreur lors de la creation de la categorie.", e);
        }
    }

    public void update(Categorie cat) {
        String sql = "UPDATE categorie SET nom=?, description=? WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cat.getNom());
            ps.setString(2, cat.getDescription());
            ps.setInt(3, cat.getId());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AppException("Cette categorie existe deja.", e);
        } catch (SQLException e) {
            throw new AppException("Erreur lors de la mise a jour de la categorie.", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM categorie WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Impossible de supprimer cette categorie.", e);
        }
    }
}
