package com.pharmacie.dao;

import com.pharmacie.config.ConnectionManager;
import com.pharmacie.model.LigneVente;
import com.pharmacie.model.ModePaiement;
import com.pharmacie.model.Vente;
import com.pharmacie.util.AppException;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acces aux donnees des tables vente et ligne_vente.
 */
public class VenteDao {

    private static final String SELECT_BASE =
            "SELECT v.*, c.nom AS client_nom, c.prenom AS client_prenom, u.nom_complet AS utilisateur_nom " +
            "FROM vente v " +
            "LEFT JOIN client c ON v.client_id = c.id " +
            "LEFT JOIN utilisateur u ON v.utilisateur_id = u.id ";

    private Vente map(ResultSet rs) throws SQLException {
        Vente v = new Vente();
        v.setId(rs.getInt("id"));
        v.setReference(rs.getString("reference"));
        int cId = rs.getInt("client_id");
        v.setClientId(rs.wasNull() ? null : cId);
        String nom = rs.getString("client_nom");
        if (nom != null) {
            String prenom = rs.getString("client_prenom");
            v.setClientNom(((prenom == null ? "" : prenom + " ") + nom).trim());
        } else {
            v.setClientNom("Client anonyme");
        }
        int uId = rs.getInt("utilisateur_id");
        v.setUtilisateurId(rs.wasNull() ? null : uId);
        v.setUtilisateurNom(rs.getString("utilisateur_nom"));
        Timestamp ts = rs.getTimestamp("date_vente");
        if (ts != null) v.setDateVente(ts.toLocalDateTime());
        v.setMontantTotal(rs.getBigDecimal("montant_total"));
        v.setModePaiement(ModePaiement.valueOf(rs.getString("mode_paiement")));
        return v;
    }

    public List<Vente> findAll() {
        String sql = SELECT_BASE + "ORDER BY v.date_vente DESC";
        return query(sql, null);
    }

    public List<Vente> findByPeriode(LocalDate debut, LocalDate fin) {
        String sql = SELECT_BASE + "WHERE DATE(v.date_vente) BETWEEN ? AND ? ORDER BY v.date_vente DESC";
        List<Vente> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(debut));
            ps.setDate(2, Date.valueOf(fin));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors du chargement des ventes de la periode.", e);
        }
        return list;
    }

    public List<Vente> search(String terme) {
        String sql = SELECT_BASE + "WHERE v.reference LIKE ? OR c.nom LIKE ? OR c.prenom LIKE ? ORDER BY v.date_vente DESC";
        List<Vente> list = new ArrayList<>();
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
            throw new AppException("Erreur lors de la recherche de ventes.", e);
        }
        return list;
    }

    public List<Vente> findByClient(int clientId) {
        String sql = SELECT_BASE + "WHERE v.client_id=? ORDER BY v.date_vente DESC";
        List<Vente> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors du chargement des ventes du client.", e);
        }
        return list;
    }

    private List<Vente> query(String sql, Integer param) {
        List<Vente> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (param != null) ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors du chargement des ventes.", e);
        }
        return list;
    }

    public Vente findById(int id) {
        Vente v = query(SELECT_BASE + "WHERE v.id=?", id).stream().findFirst().orElse(null);
        if (v != null) v.setLignes(findLignes(id));
        return v;
    }

    public List<LigneVente> findLignes(int venteId) {
        String sql = "SELECT lv.*, m.nom AS medicament_nom, m.reference AS reference FROM ligne_vente lv " +
                "JOIN medicament m ON lv.medicament_id = m.id WHERE lv.vente_id=?";
        List<LigneVente> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, venteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LigneVente l = new LigneVente();
                    l.setId(rs.getInt("id"));
                    l.setVenteId(rs.getInt("vente_id"));
                    l.setMedicamentId(rs.getInt("medicament_id"));
                    l.setMedicamentNom(rs.getString("medicament_nom"));
                    l.setReference(rs.getString("reference"));
                    l.setQuantite(rs.getInt("quantite"));
                    l.setPrixUnitaire(rs.getBigDecimal("prix_unitaire"));
                    l.setSousTotal(rs.getBigDecimal("sous_total"));
                    list.add(l);
                }
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors du chargement des lignes de vente.", e);
        }
        return list;
    }

    public String genererReference() {
        String sql = "SELECT COUNT(*) FROM vente";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int n = rs.next() ? rs.getInt(1) : 0;
            return String.format("V-%s-%05d",
                    java.time.Year.now().getValue(), n + 1);
        } catch (SQLException e) {
            return "V-" + System.currentTimeMillis();
        }
    }

    // ----- Statistiques -----

    public BigDecimal chiffreAffaires(LocalDate debut, LocalDate fin) {
        String sql = "SELECT COALESCE(SUM(montant_total),0) FROM vente WHERE DATE(date_vente) BETWEEN ? AND ?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(debut));
            ps.setDate(2, Date.valueOf(fin));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors du calcul du chiffre d'affaires.", e);
        }
    }

    public int countVentes(LocalDate debut, LocalDate fin) {
        String sql = "SELECT COUNT(*) FROM vente WHERE DATE(date_vente) BETWEEN ? AND ?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(debut));
            ps.setDate(2, Date.valueOf(fin));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors du comptage des ventes.", e);
        }
    }

    /** Top des medicaments les plus vendus sur une periode. Retourne [nom, quantite, montant]. */
    public List<Object[]> topMedicaments(LocalDate debut, LocalDate fin, int limite) {
        String sql = "SELECT m.nom, SUM(lv.quantite) AS q, SUM(lv.sous_total) AS montant " +
                "FROM ligne_vente lv JOIN vente v ON lv.vente_id = v.id " +
                "JOIN medicament m ON lv.medicament_id = m.id " +
                "WHERE DATE(v.date_vente) BETWEEN ? AND ? " +
                "GROUP BY m.id, m.nom ORDER BY q DESC LIMIT ?";
        List<Object[]> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(debut));
            ps.setDate(2, Date.valueOf(fin));
            ps.setInt(3, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{rs.getString("nom"), rs.getInt("q"), rs.getBigDecimal("montant")});
                }
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors du calcul des meilleures ventes.", e);
        }
        return list;
    }

    /** Chiffre d'affaires par jour sur une periode. Retourne [date(String), montant]. */
    public List<Object[]> chiffreAffairesParJour(LocalDate debut, LocalDate fin) {
        String sql = "SELECT DATE(date_vente) AS jour, SUM(montant_total) AS montant " +
                "FROM vente WHERE DATE(date_vente) BETWEEN ? AND ? " +
                "GROUP BY DATE(date_vente) ORDER BY jour";
        List<Object[]> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(debut));
            ps.setDate(2, Date.valueOf(fin));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{rs.getDate("jour").toLocalDate(), rs.getBigDecimal("montant")});
                }
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors du calcul du CA par jour.", e);
        }
        return list;
    }

    // ----- Methodes transactionnelles -----

    public int insertEntete(Connection c, Vente v) throws SQLException {
        String sql = "INSERT INTO vente (reference, client_id, utilisateur_id, montant_total, mode_paiement) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getReference());
            if (v.getClientId() != null) ps.setInt(2, v.getClientId());
            else ps.setNull(2, Types.INTEGER);
            if (v.getUtilisateurId() != null) ps.setInt(3, v.getUtilisateurId());
            else ps.setNull(3, Types.INTEGER);
            ps.setBigDecimal(4, v.getMontantTotal());
            ps.setString(5, v.getModePaiement().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) v.setId(keys.getInt(1));
            }
            return v.getId();
        }
    }

    public void insertLigne(Connection c, LigneVente l) throws SQLException {
        String sql = "INSERT INTO ligne_vente (vente_id, medicament_id, quantite, prix_unitaire, sous_total) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, l.getVenteId());
            ps.setInt(2, l.getMedicamentId());
            ps.setInt(3, l.getQuantite());
            ps.setBigDecimal(4, l.getPrixUnitaire());
            ps.setBigDecimal(5, l.getSousTotal());
            ps.executeUpdate();
        }
    }
}
