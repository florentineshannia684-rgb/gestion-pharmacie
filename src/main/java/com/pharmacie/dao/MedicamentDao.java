package com.pharmacie.dao;

import com.pharmacie.config.ConnectionManager;
import com.pharmacie.model.Medicament;
import com.pharmacie.util.AppException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acces aux donnees de la table medicament (avec jointure sur categorie).
 */
public class MedicamentDao {

    private static final String SELECT_BASE =
            "SELECT m.*, c.nom AS categorie_nom FROM medicament m " +
            "LEFT JOIN categorie c ON m.categorie_id = c.id ";

    private Medicament map(ResultSet rs) throws SQLException {
        Medicament m = new Medicament();
        m.setId(rs.getInt("id"));
        m.setReference(rs.getString("reference"));
        m.setNom(rs.getString("nom"));
        m.setForme(rs.getString("forme"));
        m.setDosage(rs.getString("dosage"));
        m.setPrixVente(rs.getBigDecimal("prix_vente"));
        m.setQuantiteStock(rs.getInt("quantite_stock"));
        m.setSeuilStock(rs.getInt("seuil_stock"));
        int catId = rs.getInt("categorie_id");
        m.setCategorieId(rs.wasNull() ? null : catId);
        m.setCategorieNom(rs.getString("categorie_nom"));
        m.setImagePath(rs.getString("image_path"));
        Timestamp ts = rs.getTimestamp("date_creation");
        if (ts != null) m.setDateCreation(ts.toLocalDateTime());
        return m;
    }

    public List<Medicament> findAll() {
        String sql = SELECT_BASE + "ORDER BY m.nom";
        List<Medicament> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new AppException("Erreur lors du chargement des medicaments.", e);
        }
        return list;
    }

    /** Recherche par nom/reference et filtre facultatif par categorie (null = toutes). */
    public List<Medicament> search(String terme, Integer categorieId) {
        StringBuilder sql = new StringBuilder(SELECT_BASE +
                "WHERE (m.nom LIKE ? OR m.reference LIKE ?) ");
        if (categorieId != null) sql.append("AND m.categorie_id = ? ");
        sql.append("ORDER BY m.nom");

        List<Medicament> list = new ArrayList<>();
        String like = "%" + (terme == null ? "" : terme) + "%";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            ps.setString(1, like);
            ps.setString(2, like);
            if (categorieId != null) ps.setInt(3, categorieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors de la recherche de medicaments.", e);
        }
        return list;
    }

    public Medicament findById(int id) {
        String sql = SELECT_BASE + "WHERE m.id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new AppException("Erreur lors de la lecture du medicament.", e);
        }
    }

    public List<Medicament> findStockFaible() {
        String sql = SELECT_BASE + "WHERE m.quantite_stock <= m.seuil_stock ORDER BY m.quantite_stock";
        List<Medicament> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new AppException("Erreur lors du chargement des stocks faibles.", e);
        }
        return list;
    }

    public int count() {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM medicament");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new AppException("Erreur lors du comptage des medicaments.", e);
        }
    }

    public int countStockFaible() {
        String sql = "SELECT COUNT(*) FROM medicament WHERE quantite_stock <= seuil_stock";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new AppException("Erreur lors du comptage des stocks faibles.", e);
        }
    }

    public int insert(Medicament m) {
        String sql = "INSERT INTO medicament (reference, nom, forme, dosage, prix_vente, quantite_stock, seuil_stock, categorie_id, image_path) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getReference());
            ps.setString(2, m.getNom());
            ps.setString(3, m.getForme());
            ps.setString(4, m.getDosage());
            ps.setBigDecimal(5, m.getPrixVente());
            ps.setInt(6, m.getQuantiteStock());
            ps.setInt(7, m.getSeuilStock());
            if (m.getCategorieId() != null) ps.setInt(8, m.getCategorieId());
            else ps.setNull(8, Types.INTEGER);
            ps.setString(9, m.getImagePath());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) m.setId(keys.getInt(1));
            }
            return m.getId();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AppException("Cette reference de medicament existe deja.", e);
        } catch (SQLException e) {
            throw new AppException("Erreur lors de la creation du medicament.", e);
        }
    }

    /** Mise a jour des infos (ne modifie pas quantite_stock : gere par le stock). */
    public void update(Medicament m) {
        String sql = "UPDATE medicament SET reference=?, nom=?, forme=?, dosage=?, prix_vente=?, seuil_stock=?, categorie_id=?, image_path=? WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getReference());
            ps.setString(2, m.getNom());
            ps.setString(3, m.getForme());
            ps.setString(4, m.getDosage());
            ps.setBigDecimal(5, m.getPrixVente());
            ps.setInt(6, m.getSeuilStock());
            if (m.getCategorieId() != null) ps.setInt(7, m.getCategorieId());
            else ps.setNull(7, Types.INTEGER);
            ps.setString(8, m.getImagePath());
            ps.setInt(9, m.getId());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AppException("Cette reference de medicament existe deja.", e);
        } catch (SQLException e) {
            throw new AppException("Erreur lors de la mise a jour du medicament.", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM medicament WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AppException("Impossible de supprimer ce medicament : il est utilise dans des ventes ou approvisionnements.", e);
        }
    }

    // ----- Methodes transactionnelles -----

    /** Ajuste le stock agrege du medicament dans la transaction en cours. */
    public void ajusterStock(Connection c, int medicamentId, int delta) throws SQLException {
        String sql = "UPDATE medicament SET quantite_stock = quantite_stock + ? WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, medicamentId);
            ps.executeUpdate();
        }
    }

    /** Lit le stock disponible d'un medicament en verrouillant la ligne (FOR UPDATE). */
    public int getStockPourUpdate(Connection c, int medicamentId) throws SQLException {
        String sql = "SELECT quantite_stock FROM medicament WHERE id=? FOR UPDATE";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, medicamentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public String genererReference() {
        String sql = "SELECT COUNT(*) FROM medicament";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int n = rs.next() ? rs.getInt(1) : 0;
            return String.format("MED-%04d", n + 1);
        } catch (SQLException e) {
            return "MED-" + System.currentTimeMillis();
        }
    }
}
