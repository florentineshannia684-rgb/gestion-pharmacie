package com.pharmacie.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fournit des connexions JDBC a la base de donnees MySQL.
 *
 * Pour un projet de niveau L2 on ouvre une connexion par operation
 * (ou par transaction) et on la ferme via try-with-resources.
 */
public final class ConnectionManager {

    private ConnectionManager() {
    }

    static {
        try {
            Class.forName(DatabaseConfig.getInstance().getDriver());
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "Pilote JDBC MySQL introuvable : " + e.getMessage());
        }
    }

    /**
     * Ouvre une nouvelle connexion a la base de donnees.
     * @return une connexion JDBC prete a l'emploi
     * @throws SQLException si la connexion echoue
     */
    public static Connection getConnection() throws SQLException {
        DatabaseConfig cfg = DatabaseConfig.getInstance();
        return DriverManager.getConnection(cfg.getUrl(), cfg.getUser(), cfg.getPassword());
    }

    /**
     * Teste la connexion a la base de donnees.
     * @return true si la connexion reussit
     */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            System.err.println("Echec de la connexion a la base : " + e.getMessage());
            return false;
        }
    }
}
