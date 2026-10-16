package com.pharmacie.dao;

import com.pharmacie.config.ConnectionManager;
import com.pharmacie.model.Approvisionnement;
import com.pharmacie.model.LigneApprovisionnement;
import com.pharmacie.util.AppException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acces aux donnees des tables approvisionnement et ligne_approvisionnement.
 */
public class ApprovisionnementDao {

    private static final String SELECT_BASE =
            "SELECT a.*, f.nom AS fournisseur_nom, u.nom_complet AS utilisateur_nom " +
            "FROM approvisionnement a " +
            "LEFT JOIN fournisseur f ON a.fournisseur_id = f.id " +
            "LEFT JOIN utilisateur u ON a.utilisateur_id = u.id ";

    private Approvisionnement map(ResultSet rs) throws SQLException {
        Approvisionnement a = new Approvisionnement();
        a.setId(rs.getInt("id"));
        a.setReference(rs.getString("reference"));
        int fId = rs.getInt("fournisseur_id");
        a.setFournisseurId(rs.wasNull() ? null : fId);
        a.setFournisseurNom(rs.getString("fournisseur_nom"));
        int uId = rs.getInt("utilisateur_id");
        a.setUtilisateurId(rs.wasNull() ? null : uId);
        a.setUtilisateurNom(rs.getString("utilisateur_nom"));
        Timestamp ts = rs.getTimestamp("date_appro");
        if (ts != null) a.setDateAppro(ts.toLocalDateTime());
        a.setMontantTotal(rs.getBigDecimal("montant_total"));
        return a;
    }

    public List<Approvisionnement> findAll() {
        String sql = SELECT_BASE + "ORDER BY a.date_appro DESC";
        List<Approvisionnement> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new AppException("Erreur lors du chargement des approvisionnements.", e);
        }
        return list;
    }

    public List<Approvisionnement> findByFournisseur(int fournisseurId) {
        String sql = SELECT_BASE + "WHERE a.fournisseur_id=? ORDER BY a.date_appro DESC";
        List<Approvisionnement> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, fournisseurId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors du chargement des approvisionnements du fournisseur.", e);
        }
        return list;
    }

    public List<LigneApprovisionnement> findLignes(int approId) {
        String sql = "SELECT la.*, m.nom AS medicament_nom FROM ligne_approvisionnement la " +
                "JOIN medicament m ON la.medicament_id = m.id WHERE la.approvisionnement_id=?";
        List<LigneApprovisionnement> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, approId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LigneApprovisionnement l = new LigneApprovisionnement();
                    l.setId(rs.getInt("id"));
                    l.setApprovisionnementId(rs.getInt("approvisionnement_id"));
                    l.setMedicamentId(rs.getInt("medicament_id"));
                    l.setMedicamentNom(rs.getString("medicament_nom"));
                    int lotId = rs.getInt("lot_id");
                    l.setLotId(rs.wasNull() ? null : lotId);
                    l.setQuantite(rs.getInt("quantite"));
                    l.setPrixAchat(rs.getBigDecimal("prix_achat"));
                    l.setNumeroLot(rs.getString("numero_lot"));
                    Date de = rs.getDate("date_expiration");
                    if (de != null) l.setDateExpiration(de.toLocalDate());
                    list.add(l);
                }
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors du chargement des lignes d'approvisionnement.", e);
        }
        return list;
    }

    public String genererReference() {
        String sql = "SELECT COUNT(*) FROM approvisionnement";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int n = rs.next() ? rs.getInt(1) : 0;
            return String.format("APP-%06d", n + 1);
        } catch (SQLException e) {
            return "APP-" + System.currentTimeMillis();
        }
    }

    // ----- Methodes transactionnelles -----

    public int insertEntete(Connection c, Approvisionnement a) throws SQLException {
        String sql = "INSERT INTO approvisionnement (reference, fournisseur_id, utilisateur_id, montant_total) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getReference());
            if (a.getFournisseurId() != null) ps.setInt(2, a.getFournisseurId());
            else ps.setNull(2, Types.INTEGER);
            if (a.getUtilisateurId() != null) ps.setInt(3, a.getUtilisateurId());
            else ps.setNull(3, Types.INTEGER);
            ps.setBigDecimal(4, a.getMontantTotal());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) a.setId(keys.getInt(1));
            }
            return a.getId();
        }
    }

    public void insertLigne(Connection c, LigneApprovisionnement l) throws SQLException {
        String sql = "INSERT INTO ligne_approvisionnement (approvisionnement_id, medicament_id, lot_id, quantite, prix_achat, numero_lot, date_expiration) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, l.getApprovisionnementId());
            ps.setInt(2, l.getMedicamentId());
            if (l.getLotId() != null) ps.setInt(3, l.getLotId());
            else ps.setNull(3, Types.INTEGER);
            ps.setInt(4, l.getQuantite());
            ps.setBigDecimal(5, l.getPrixAchat());
            ps.setString(6, l.getNumeroLot());
            if (l.getDateExpiration() != null) ps.setDate(7, Date.valueOf(l.getDateExpiration()));
            else ps.setNull(7, Types.DATE);
            ps.executeUpdate();
        }
    }
}
