package com.pharmacie.dao;

import com.pharmacie.config.ConnectionManager;
import com.pharmacie.model.Lot;
import com.pharmacie.util.AppException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acces aux donnees de la table lot.
 * Certaines methodes acceptent une Connection pour participer a une transaction.
 */
public class LotDao {

    private static final String SELECT_BASE =
            "SELECT l.*, m.nom AS medicament_nom FROM lot l " +
            "JOIN medicament m ON l.medicament_id = m.id ";

    private Lot map(ResultSet rs) throws SQLException {
        Lot l = new Lot();
        l.setId(rs.getInt("id"));
        l.setMedicamentId(rs.getInt("medicament_id"));
        l.setMedicamentNom(rs.getString("medicament_nom"));
        l.setNumeroLot(rs.getString("numero_lot"));
        l.setQuantite(rs.getInt("quantite"));
        l.setPrixAchat(rs.getBigDecimal("prix_achat"));
        Date de = rs.getDate("date_expiration");
        if (de != null) l.setDateExpiration(de.toLocalDate());
        Timestamp ts = rs.getTimestamp("date_reception");
        if (ts != null) l.setDateReception(ts.toLocalDateTime());
        return l;
    }

    public List<Lot> findAll() {
        String sql = SELECT_BASE + "ORDER BY l.date_expiration IS NULL, l.date_expiration";
        List<Lot> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new AppException("Erreur lors du chargement des lots.", e);
        }
        return list;
    }

    public List<Lot> findByMedicament(int medicamentId) {
        String sql = SELECT_BASE + "WHERE l.medicament_id=? ORDER BY l.date_expiration IS NULL, l.date_expiration";
        List<Lot> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, medicamentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors du chargement des lots du medicament.", e);
        }
        return list;
    }

    /** Lots proches de l'expiration (dans seuilJours) et non expires, quantite > 0. */
    public List<Lot> findProchesExpiration(int seuilJours) {
        String sql = SELECT_BASE +
                "WHERE l.quantite > 0 AND l.date_expiration IS NOT NULL " +
                "AND l.date_expiration <= DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                "ORDER BY l.date_expiration";
        List<Lot> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, seuilJours);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors du chargement des lots proches de l'expiration.", e);
        }
        return list;
    }

    public int countProchesExpiration(int seuilJours) {
        String sql = "SELECT COUNT(*) FROM lot WHERE quantite > 0 AND date_expiration IS NOT NULL " +
                "AND date_expiration <= DATE_ADD(CURDATE(), INTERVAL ? DAY)";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, seuilJours);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors du comptage des lots proches de l'expiration.", e);
        }
    }

    // ----- Methodes transactionnelles (utilisent une Connection partagee) -----

    /**
     * Cherche un lot existant (meme medicament + meme numero de lot) dans la transaction.
     * @return l'id du lot ou null si absent
     */
    public Integer findLotId(Connection c, int medicamentId, String numeroLot) throws SQLException {
        String sql = "SELECT id FROM lot WHERE medicament_id=? AND numero_lot=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, medicamentId);
            ps.setString(2, numeroLot);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    public int insert(Connection c, Lot lot) throws SQLException {
        String sql = "INSERT INTO lot (medicament_id, numero_lot, quantite, prix_achat, date_expiration) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, lot.getMedicamentId());
            ps.setString(2, lot.getNumeroLot());
            ps.setInt(3, lot.getQuantite());
            ps.setBigDecimal(4, lot.getPrixAchat());
            if (lot.getDateExpiration() != null) ps.setDate(5, Date.valueOf(lot.getDateExpiration()));
            else ps.setNull(5, Types.DATE);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) lot.setId(keys.getInt(1));
            }
            return lot.getId();
        }
    }

    public void ajouterQuantite(Connection c, int lotId, int delta) throws SQLException {
        String sql = "UPDATE lot SET quantite = quantite + ? WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, lotId);
            ps.executeUpdate();
        }
    }

    /**
     * Retourne les lots d'un medicament ayant du stock, tries par expiration (FIFO),
     * verrouilles pour mise a jour (FOR UPDATE) dans la transaction en cours.
     */
    public List<Lot> findLotsAvecStockPourVente(Connection c, int medicamentId) throws SQLException {
        String sql = "SELECT * FROM lot WHERE medicament_id=? AND quantite > 0 " +
                "ORDER BY date_expiration IS NULL, date_expiration FOR UPDATE";
        List<Lot> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, medicamentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Lot l = new Lot();
                    l.setId(rs.getInt("id"));
                    l.setMedicamentId(rs.getInt("medicament_id"));
                    l.setNumeroLot(rs.getString("numero_lot"));
                    l.setQuantite(rs.getInt("quantite"));
                    Date de = rs.getDate("date_expiration");
                    if (de != null) l.setDateExpiration(de.toLocalDate());
                    list.add(l);
                }
            }
        }
        return list;
    }

    public void retirerQuantite(Connection c, int lotId, int delta) throws SQLException {
        String sql = "UPDATE lot SET quantite = quantite - ? WHERE id=? AND quantite >= ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, lotId);
            ps.setInt(3, delta);
            int n = ps.executeUpdate();
            if (n == 0) {
                throw new SQLException("Quantite insuffisante dans le lot " + lotId);
            }
        }
    }
}
