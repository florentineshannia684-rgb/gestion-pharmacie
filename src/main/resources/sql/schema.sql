-- =====================================================================
--  SYSTEME DE GESTION D'UNE PHARMACIE
--  Script de creation et d'initialisation de la base de donnees MySQL
--  Encodage : UTF-8 / utf8mb4
-- =====================================================================

DROP DATABASE IF EXISTS pharmacie;
CREATE DATABASE pharmacie
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE pharmacie;

-- =====================================================================
--  TABLE : utilisateur
--  Comptes du personnel et roles d'acces
-- =====================================================================
CREATE TABLE utilisateur (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    nom_utilisateur VARCHAR(50)  NOT NULL UNIQUE,
    mot_de_passe    VARCHAR(100) NOT NULL,            -- hash BCrypt
    nom_complet     VARCHAR(120) NOT NULL,
    role            ENUM('ADMIN','PHARMACIEN','VENDEUR') NOT NULL DEFAULT 'VENDEUR',
    actif           BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =====================================================================
--  TABLE : categorie
-- =====================================================================
CREATE TABLE categorie (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nom         VARCHAR(80) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB;

-- =====================================================================
--  TABLE : fournisseur
-- =====================================================================
CREATE TABLE fournisseur (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    nom       VARCHAR(120) NOT NULL,
    contact   VARCHAR(120),
    telephone VARCHAR(30),
    email     VARCHAR(120),
    adresse   VARCHAR(255)
) ENGINE=InnoDB;

-- =====================================================================
--  TABLE : client
--  Le client n'est pas un utilisateur : infos saisies pour la vente/recu
-- =====================================================================
CREATE TABLE client (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    nom       VARCHAR(80) NOT NULL,
    prenom    VARCHAR(80),
    telephone VARCHAR(30),
    email     VARCHAR(120),
    adresse   VARCHAR(255)
) ENGINE=InnoDB;

-- =====================================================================
--  TABLE : medicament
--  quantite_stock = cache agrege (somme des lots), tenu a jour par transaction
-- =====================================================================
CREATE TABLE medicament (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    reference      VARCHAR(50)  NOT NULL UNIQUE,
    nom            VARCHAR(150) NOT NULL,
    forme          VARCHAR(80),                 -- comprime, sirop, injectable...
    dosage         VARCHAR(80),                 -- 500mg, 1g...
    prix_vente     DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (prix_vente >= 0),
    quantite_stock INT NOT NULL DEFAULT 0 CHECK (quantite_stock >= 0),
    seuil_stock    INT NOT NULL DEFAULT 10 CHECK (seuil_stock >= 0),
    categorie_id   INT,
    image_path     VARCHAR(255),
    date_creation  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medicament_categorie
        FOREIGN KEY (categorie_id) REFERENCES categorie(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_medicament_nom (nom),
    INDEX idx_medicament_categorie (categorie_id)
) ENGINE=InnoDB;

-- =====================================================================
--  TABLE : lot
--  Suivi des dates d'expiration au niveau du lot
-- =====================================================================
CREATE TABLE lot (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    medicament_id   INT NOT NULL,
    numero_lot      VARCHAR(60) NOT NULL,
    quantite        INT NOT NULL DEFAULT 0 CHECK (quantite >= 0),
    prix_achat      DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (prix_achat >= 0),
    date_expiration DATE,
    date_reception  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lot_medicament
        FOREIGN KEY (medicament_id) REFERENCES medicament(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_lot_medicament (medicament_id),
    INDEX idx_lot_expiration (date_expiration)
) ENGINE=InnoDB;

-- =====================================================================
--  TABLE : approvisionnement  (entete)
-- =====================================================================
CREATE TABLE approvisionnement (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    reference      VARCHAR(50) NOT NULL UNIQUE,
    fournisseur_id INT,
    utilisateur_id INT,
    date_appro     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    montant_total  DECIMAL(14,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_appro_fournisseur
        FOREIGN KEY (fournisseur_id) REFERENCES fournisseur(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_appro_utilisateur
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_appro_date (date_appro)
) ENGINE=InnoDB;

-- =====================================================================
--  TABLE : ligne_approvisionnement  (detail)
-- =====================================================================
CREATE TABLE ligne_approvisionnement (
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    approvisionnement_id INT NOT NULL,
    medicament_id        INT NOT NULL,
    lot_id               INT,
    quantite             INT NOT NULL CHECK (quantite > 0),
    prix_achat           DECIMAL(12,2) NOT NULL DEFAULT 0,
    numero_lot           VARCHAR(60),
    date_expiration      DATE,
    CONSTRAINT fk_ligneappro_appro
        FOREIGN KEY (approvisionnement_id) REFERENCES approvisionnement(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ligneappro_medicament
        FOREIGN KEY (medicament_id) REFERENCES medicament(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_ligneappro_lot
        FOREIGN KEY (lot_id) REFERENCES lot(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_ligneappro_appro (approvisionnement_id)
) ENGINE=InnoDB;

-- =====================================================================
--  TABLE : vente  (entete)
-- =====================================================================
CREATE TABLE vente (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    reference      VARCHAR(50) NOT NULL UNIQUE,
    client_id      INT,
    utilisateur_id INT,
    date_vente     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    montant_total  DECIMAL(14,2) NOT NULL DEFAULT 0,
    mode_paiement  ENUM('ESPECES','CARTE','MOBILE','AUTRE') NOT NULL DEFAULT 'ESPECES',
    CONSTRAINT fk_vente_client
        FOREIGN KEY (client_id) REFERENCES client(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_vente_utilisateur
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_vente_date (date_vente),
    INDEX idx_vente_client (client_id)
) ENGINE=InnoDB;

-- =====================================================================
--  TABLE : ligne_vente  (detail)
-- =====================================================================
CREATE TABLE ligne_vente (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    vente_id      INT NOT NULL,
    medicament_id INT NOT NULL,
    quantite      INT NOT NULL CHECK (quantite > 0),
    prix_unitaire DECIMAL(12,2) NOT NULL DEFAULT 0,
    sous_total    DECIMAL(14,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_lignevente_vente
        FOREIGN KEY (vente_id) REFERENCES vente(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_lignevente_medicament
        FOREIGN KEY (medicament_id) REFERENCES medicament(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_lignevente_vente (vente_id)
) ENGINE=InnoDB;

-- =====================================================================
--  TABLE : mouvement_stock
--  Historique de toutes les entrees / sorties
-- =====================================================================
CREATE TABLE mouvement_stock (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    medicament_id  INT NOT NULL,
    lot_id         INT,
    type_mouvement ENUM('ENTREE','SORTIE','AJUSTEMENT') NOT NULL,
    quantite       INT NOT NULL,
    source         VARCHAR(80),           -- ex : reference appro/vente
    utilisateur_id INT,
    date_mouvement DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mouvement_medicament
        FOREIGN KEY (medicament_id) REFERENCES medicament(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_mouvement_lot
        FOREIGN KEY (lot_id) REFERENCES lot(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_mouvement_utilisateur
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_mouvement_medicament (medicament_id),
    INDEX idx_mouvement_date (date_mouvement)
) ENGINE=InnoDB;

-- =====================================================================
--  DONNEES INITIALES
-- =====================================================================

-- Comptes par defaut (mot de passe BCrypt).
-- admin    / admin123   -> role ADMIN
-- pharma   / pharma123  -> role PHARMACIEN
-- vendeur  / vendeur123 -> role VENDEUR
-- Les hash ci-dessous sont des hash BCrypt valides des mots de passe indiques.
INSERT INTO utilisateur (nom_utilisateur, mot_de_passe, nom_complet, role) VALUES
('admin',   '$2a$10$N9qo8uLOickgx2ZMRZoMy.MH/rcDkCfK4oM5mY1rN1hQ3B6yy1xWu', 'Administrateur Systeme', 'ADMIN'),
('pharma',  '$2a$10$N9qo8uLOickgx2ZMRZoMy.MH/rcDkCfK4oM5mY1rN1hQ3B6yy1xWu', 'Pharmacien Principal',   'PHARMACIEN'),
('vendeur', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MH/rcDkCfK4oM5mY1rN1hQ3B6yy1xWu', 'Vendeur Comptoir',       'VENDEUR');
-- NB : ces hash sont des espaces reserves ; l'application regenere les comptes
--      par defaut au premier demarrage si la table est vide ou si le hash est invalide.

INSERT INTO categorie (nom, description) VALUES
('Antalgiques',         'Medicaments contre la douleur'),
('Antibiotiques',       'Traitement des infections bacteriennes'),
('Anti-inflammatoires', 'Reduction de l inflammation'),
('Vitamines',           'Complements vitamines et mineraux'),
('Dermatologie',        'Soins de la peau'),
('Digestion',           'Troubles digestifs'),
('Cardiologie',         'Systeme cardio-vasculaire'),
('Respiratoire',        'Voies respiratoires et allergies');

INSERT INTO fournisseur (nom, contact, telephone, email, adresse) VALUES
('PharmaDist SARL', 'M. Rakoto',      '034 12 345 67', 'contact@pharmadist.mg', 'Antananarivo'),
('MediGros',        'Mme Rasoa',      '032 98 765 43', 'ventes@medigros.mg',    'Fianarantsoa'),
('BioSante',        'M. Jean',        '033 11 222 33', 'info@biosante.mg',      'Toamasina'),
('Ocean Indien Pharma','Mme Hary',    '034 55 666 77', 'achats@oipharma.mg',    'Mahajanga'),
('Global Medic',    'M. Andria',      '032 44 333 22', 'global@medic.mg',       'Antsirabe');

INSERT INTO client (nom, prenom, telephone, email, adresse) VALUES
('Randria',    'Hasina',   '034 10 203 04', 'hasina.r@mail.mg',   'Lot II Ambatomena'),
('Rakoto',     'Fanja',    '032 20 304 05', 'fanja.rk@mail.mg',   'Cite Analakely'),
('Rabe',       'Tojo',     '033 30 405 06', NULL,                 'Ampefiloha'),
('Razafy',     'Mialy',    '034 40 506 07', 'mialy.rz@mail.mg',   'Isotry'),
('Andriana',   'Naina',    '032 50 607 08', NULL,                 'Andravoahangy'),
('Ravelo',     'Soa',      '033 60 708 09', 'soa.ravelo@mail.mg', 'Tsaralalana');

INSERT INTO medicament (reference, nom, forme, dosage, prix_vente, quantite_stock, seuil_stock, categorie_id) VALUES
('MED-0001', 'Paracetamol',        'Comprime', '500mg',  500.00,  0, 20, 1),
('MED-0002', 'Ibuprofene',         'Comprime', '400mg',  800.00,  0, 15, 3),
('MED-0003', 'Amoxicilline',       'Gelule',   '500mg',  1500.00, 0, 10, 2),
('MED-0004', 'Vitamine C',         'Comprime', '1g',     300.00,  0, 30, 4),
('MED-0005', 'Aspirine',           'Comprime', '500mg',  400.00,  0, 20, 1),
('MED-0006', 'Smecta',             'Sachet',   '3g',     600.00,  0, 25, 6),
('MED-0007', 'Doliprane',          'Comprime', '1000mg', 700.00,  0, 20, 1),
('MED-0008', 'Azithromycine',      'Comprime', '250mg',  2500.00, 0, 10, 2),
('MED-0009', 'Cetirizine',         'Comprime', '10mg',   900.00,  0, 15, 8),
('MED-0010', 'Omeprazole',         'Gelule',   '20mg',   1200.00, 0, 15, 6),
('MED-0011', 'Multivitamines',     'Comprime', '-',      1800.00, 0, 20, 4),
('MED-0012', 'Betadine',           'Solution', '10%',    3500.00, 0, 10, 5),
('MED-0013', 'Amlodipine',         'Comprime', '5mg',    1600.00, 0, 12, 7),
('MED-0014', 'Metformine',         'Comprime', '850mg',  1400.00, 0, 15, 6),
('MED-0015', 'Creme hydratante',   'Tube',     '-',      4000.00, 0, 8,  5),
('MED-0016', 'Ventoline',          'Spray',    '100ug',  5500.00, 0, 10, 8);

-- Fin du script
-- NB : les quantites en stock, les lots, les approvisionnements et les ventes
--      de demonstration sont generes par l'outil com.pharmacie.tools.DemoDataSeeder
--      (via les services, pour garantir la coherence du stock et de l'historique).
