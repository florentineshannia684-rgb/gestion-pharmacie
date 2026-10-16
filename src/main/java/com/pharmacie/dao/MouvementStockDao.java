package com.pharmacie.dao;

import com.pharmacie.config.ConnectionManager;
import com.pharmacie.model.MouvementStock;
import com.pharmacie.model.TypeMouvement;
import com.pharmacie.util.AppException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acces aux donnees de la table mouvement_stock (historique entrees/sorties).
 */
public class MouvementStockDao {

    private static final String SELECT_BASE =
            "SELECT ms.*, m.nom AS medicament_nom, l.numero_lot AS numero_lot, u.nom_complet AS utilisateur_nom " +
            "FROM mouvement_stock ms " +
            "JOIN medicament m ON ms.medicament_id = m.id " +
            "LEFT JOIN lot l ON ms.lot_id = l.id " +
            "LEFT JOIN utilisateur u ON ms.utilisateur_id = u.id ";

    private MouvementStock map(ResultSet rs) throws SQLException {
        MouvementStock ms = new MouvementStock();
        ms.setId(rs.getInt("id"));
        ms.setMedicamentId(rs.getInt("medicament_id"));
        ms.setMedicamentNom(rs.getString("medicament_nom"));
        int lotId = rs.getInt("lot_id");
        ms.setLotId(rs.wasNull() ? null : lotId);
        ms.setNumeroLot(rs.getString("numero_lot"));
        ms.setTypeMouvement(TypeMouvement.valueOf(rs.getString("type_mouvement")));
        ms.setQuantite(rs.getInt("quantite"));
        ms.setSource(rs.getString("source"));
        int uId = rs.getInt("utilisateur_id");
        ms.setUtilisateurId(rs.wasNull() ? null : uId);
        ms.setUtilisateurNom(rs.getString("utilisateur_nom"));
        Timestamp ts = rs.getTimestamp("date_mouvement");
        if (ts != null) ms.setDateMouvement(ts.toLocalDateTime());
        return ms;
    }

    public List<MouvementStock> findRecents(int limite) {
        String sql = SELECT_BASE + "ORDER BY ms.date_mouvement DESC LIMIT ?";
        List<MouvementStock> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors du chargement des mouvements de stock.", e);
        }
        return list;
    }

    public List<MouvementStock> findByMedicament(int medicamentId) {
        String sql = SELECT_BASE + "WHERE ms.medicament_id=? ORDER BY ms.date_mouvement DESC";
        List<MouvementStock> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, medicamentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors du chargement des mouvements du medicament.", e);
        }
        return list;
    }

    /** Insertion transactionnelle d'un mouvement. */
    public void insert(Connection c, MouvementStock ms) throws SQLException {
        String sql = "INSERT INTO mouvement_stock (medicament_id, lot_id, type_mouvement, quantite, source, utilisateur_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ms.getMedicamentId());
            if (ms.getLotId() != null) ps.setInt(2, ms.getLotId());
            else ps.setNull(2, Types.INTEGER);
            ps.setString(3, ms.getTypeMouvement().name());
            ps.setInt(4, ms.getQuantite());
            ps.setString(5, ms.getSource());
            if (ms.getUtilisateurId() != null) ps.setInt(6, ms.getUtilisateurId());
            else ps.setNull(6, Types.INTEGER);
            ps.executeUpdate();
        }
    }
}
