package com.pharmacie.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Charge la configuration de la base de donnees.
 *
 * Ordre de priorite :
 *   1. Variables d'environnement (DB_URL, DB_USER, DB_PASSWORD)
 *   2. Fichier externe "db.properties" place a cote de l'application
 *   3. Fichier "db.properties" embarque dans les ressources
 *
 * Aucun identifiant n'est code en dur dans le code source.
 */
public final class DatabaseConfig {

    private static final String RESOURCE = "/db.properties";
    private static DatabaseConfig instance;

    private final String url;
    private final String user;
    private final String password;
    private final String driver;

    private DatabaseConfig() {
        Properties props = new Properties();

        // 3. Ressource embarquee (valeurs par defaut)
        try (InputStream in = DatabaseConfig.class.getResourceAsStream(RESOURCE)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            System.err.println("Impossible de lire db.properties embarque : " + e.getMessage());
        }

        // 2. Fichier externe (facultatif) a cote du .jar / du repertoire courant
        Path external = Path.of("db.properties");
        if (Files.exists(external)) {
            try (InputStream in = Files.newInputStream(external)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("Impossible de lire le db.properties externe : " + e.getMessage());
            }
        }

        // 1. Variables d'environnement (prioritaires)
        this.url = firstNonBlank(System.getenv("DB_URL"), props.getProperty("db.url"));
        this.user = firstNonBlank(System.getenv("DB_USER"), props.getProperty("db.user", "root"));
        this.password = firstNonNull(System.getenv("DB_PASSWORD"), props.getProperty("db.password", ""));
        this.driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    private static String firstNonBlank(String a, String b) {
        return (a != null && !a.isBlank()) ? a : b;
    }

    private static String firstNonNull(String a, String b) {
        return (a != null) ? a : b;
    }

    public String getUrl()      { return url; }
    public String getUser()     { return user; }
    public String getPassword() { return password; }
    public String getDriver()   { return driver; }
}
