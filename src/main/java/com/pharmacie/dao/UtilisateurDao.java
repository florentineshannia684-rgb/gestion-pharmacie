package com.pharmacie.dao;

import com.pharmacie.config.ConnectionManager;
import com.pharmacie.model.Role;
import com.pharmacie.model.Utilisateur;
import com.pharmacie.util.AppException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acces aux donnees de la table utilisateur.
 */
public class UtilisateurDao {

    private Utilisateur map(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id"));
        u.setNomUtilisateur(rs.getString("nom_utilisateur"));
        u.setMotDePasse(rs.getString("mot_de_passe"));
        u.setNomComplet(rs.getString("nom_complet"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setActif(rs.getBoolean("actif"));
        Timestamp ts = rs.getTimestamp("date_creation");
        if (ts != null) u.setDateCreation(ts.toLocalDateTime());
        return u;
    }

    public Utilisateur findByNomUtilisateur(String nomUtilisateur) {
        String sql = "SELECT * FROM utilisateur WHERE nom_utilisateur = ?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nomUtilisateur);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors de la recherche de l'utilisateur.", e);
        }
    }

    public Utilisateur findById(int id) {
        String sql = "SELECT * FROM utilisateur WHERE id = ?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors de la lecture de l'utilisateur.", e);
        }
    }

    public List<Utilisateur> findAll() {
        String sql = "SELECT * FROM utilisateur ORDER BY nom_complet";
        List<Utilisateur> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new AppException("Erreur lors du chargement des utilisateurs.", e);
        }
        return list;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM utilisateur";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new AppException("Erreur lors du comptage des utilisateurs.", e);
        }
    }

    public int insert(Utilisateur u) {
        String sql = "INSERT INTO utilisateur (nom_utilisateur, mot_de_passe, nom_complet, role, actif) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNomUtilisateur());
            ps.setString(2, u.getMotDePasse());
            ps.setString(3, u.getNomComplet());
            ps.setString(4, u.getRole().name());
            ps.setBoolean(5, u.isActif());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) u.setId(keys.getInt(1));
            }
            return u.getId();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AppException("Ce nom d'utilisateur existe deja.", e);
        } catch (SQLException e) {
            throw new AppException("Erreur lors de la creation de l'utilisateur.", e);
        }
    }

    /** Met a jour les informations (sans toucher au mot de passe). */
    public void update(Utilisateur u) {
        String sql = "UPDATE utilisateur SET nom_utilisateur=?, nom_complet=?, role=?, actif=? WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getNomUtilisateur());
            ps.setString(2, u.getNomComplet());
            ps.setString(3, u.getRole().name());
            ps.setBoolean(4, u.isActif());
            ps.setInt(5, u.getId());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AppException("Ce nom d'utilisateur existe deja.", e);
        } catch (SQLException e) {
            throw new AppException("Erreur lors de la mise a jour de l'utilisateur.", e);
        }
    }

    public void updateMotDePasse(int id, String hash) {
        String sql = "UPDATE utilisateur SET mot_de_passe=? WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Erreur lors du changement de mot de passe.", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM utilisateur WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Impossible de supprimer cet utilisateur (donnees liees).", e);
        }
    }
}
