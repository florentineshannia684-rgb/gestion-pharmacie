# PharmaGest — Système de gestion d'une pharmacie

Application **desktop** de gestion interne d'une pharmacie, développée en **Java Swing** avec
une base de données **MySQL** (accès via **JDBC**, pattern **DAO**).

Elle couvre : médicaments, catégories, fournisseurs, approvisionnements, stock & lots,
clients, ventes/caisse, reçus imprimables, historique, utilisateurs/rôles et tableau de
bord statistique.

---

## 1. Prérequis

| Outil | Version conseillée | Remarque |
|-------|--------------------|----------|
| JDK   | 21 (LTS)           | Eclipse Temurin |
| Maven | 3.9+               | Gestion des dépendances |
| MySQL | 8.x (ou MariaDB via XAMPP) | Serveur de base de données |

> Sous Windows avec **XAMPP** : démarrez **MySQL** depuis le *XAMPP Control Panel*.
> L'utilisateur par défaut est `root` **sans mot de passe**.

---

## 2. Création de la base de données

Le script se trouve dans [`src/main/resources/sql/schema.sql`](src/main/resources/sql/schema.sql).
Il crée la base `pharmacie`, toutes les tables, les contraintes/index et des données d'exemple.

### Option A — en ligne de commande (XAMPP)
```bash
C:\xampp\mysql\bin\mysql.exe -u root < src/main/resources/sql/schema.sql
```

### Option B — phpMyAdmin
1. Ouvrez `http://localhost/phpmyadmin`.
2. Onglet **Importer** → choisissez `schema.sql` → **Exécuter**.

### Option C — client MySQL standard
```bash
mysql -u root -p < src/main/resources/sql/schema.sql
```

### (Recommandé) Générer des données de démonstration
Le script SQL crée le catalogue (médicaments, catégories, fournisseurs, clients) avec
un **stock à 0**. Pour peupler l'application avec un stock réaliste, des **lots**
(dont certains proches de l'expiration ou périmés), des **approvisionnements** et
**14 ventes réparties sur les 7 derniers jours** (pour un tableau de bord et des
rapports vivants), lancez l'outil fourni **après** avoir compilé (voir §4) :

```bash
java -cp target/gestion-pharmacie.jar com.pharmacie.tools.DemoDataSeeder
# ajoutez l'argument "force" pour régénérer même si des ventes existent déjà
```

Les données sont générées **via les services transactionnels**, ce qui garantit la
cohérence (stock = somme des lots, mouvements et totaux corrects). L'outil est
idempotent : il ne fait rien si des ventes existent déjà.

---

## 3. Configuration de la connexion

Les identifiants **ne sont pas codés en dur**. Ils sont lus depuis
[`src/main/resources/db.properties`](src/main/resources/db.properties) :

```properties
db.url=jdbc:mysql://localhost:3306/pharmacie?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
db.user=root
db.password=
```

Vous pouvez surcharger sans recompiler :
- via des **variables d'environnement** : `DB_URL`, `DB_USER`, `DB_PASSWORD` ;
- ou en plaçant un fichier `db.properties` **à côté du `.jar`**.

---

## 4. Compilation et lancement

### Lancer directement (développement)
```bash
mvn clean compile
mvn exec:java
```

### Construire un `.jar` exécutable autonome
```bash
mvn clean package
java -jar target/gestion-pharmacie.jar
```

Le `.jar` produit contient toutes les dépendances (plugin *shade*).

---

## 5. Comptes par défaut

Au premier démarrage, l'application garantit l'existence de ces comptes
(les mots de passe sont hachés avec **BCrypt**) :

| Identifiant | Mot de passe | Rôle | Accès |
|-------------|--------------|------|-------|
| `admin`   | `admin123`   | Administrateur | Tout + gestion des utilisateurs |
| `pharma`  | `pharma123`  | Pharmacien | Médicaments, stock, fournisseurs, appro, ventes, rapports |
| `vendeur` | `vendeur123` | Vendeur | Caisse, clients, historique, stock (consultation) |

> Changez ces mots de passe après la première connexion (module **Utilisateurs**).

---

## 6. Architecture

```
com.pharmacie
├── Main                     Point d'entrée (FlatLaf + test connexion + bootstrap)
├── config                   DatabaseConfig, ConnectionManager (JDBC)
├── model                    Entités + enums (Role, ModePaiement, TypeMouvement)
├── dao                      Accès données (PreparedStatement, transactions)
├── service                  Logique métier (ventes/appro transactionnels, stats, auth)
├── util                     Password (BCrypt), Session, Thème, Format, Helpers
└── view                     LoginFrame, MainFrame (sidebar)
    ├── components           ModulePanel, StatCard, UIFactory (composants réutilisables)
    └── panels               13 modules (dashboard, médicaments, caisse, stock, ...)
```

**Séparation des couches** : `view` → `service` → `dao` → MySQL.

**Points clés de fiabilité**
- Requêtes **paramétrées** (`PreparedStatement`) → anti-injection SQL.
- Mots de passe **hachés BCrypt** (jamais en clair).
- **Transactions** pour les ventes et approvisionnements (rollback si échec) →
  cohérence garantie du stock.
- Contrôle des droits selon le **rôle** (navigation adaptée).
- Validation des saisies, **quantités négatives interdites**, ventes bloquées
  si stock insuffisant.
- Suivi des **dates d'expiration par lot** (déduction FIFO à la vente).

---

## 7. Scénario de démonstration

1. Connectez-vous en `pharma` / `pharma123`.
2. **Médicaments** : vérifiez/ajoutez un médicament (stock initial à 0).
3. **Approvisionnements** → *Nouvel approvisionnement* : choisissez un fournisseur,
   ajoutez une ligne (médicament, quantité, prix d'achat, n° de lot, date d'expiration),
   validez → le **stock augmente** automatiquement.
4. **Stock** : constatez la quantité, le lot et le mouvement d'entrée.
5. Déconnectez-vous, reconnectez-vous en `vendeur` / `vendeur123`.
6. **Ventes / Caisse** : recherchez un médicament, ajoutez-le au panier, indiquez la
   quantité, choisissez un client (optionnel) et le mode de paiement, puis
   **Valider la vente** → le **reçu** s'affiche (imprimable) et le **stock diminue**.
7. **Historique des ventes** : retrouvez la vente et ré-ouvrez le reçu.
8. Reconnectez-vous en `admin` : **Tableau de bord**, **Rapports**, **Utilisateurs**.

---

## 8. Dépannage

| Problème | Solution |
|----------|----------|
| « Impossible de se connecter à la base » | Démarrez MySQL (XAMPP) ; vérifiez `db.properties` et que la base `pharmacie` existe. |
| Identifiant/mot de passe incorrect | Utilisez les comptes par défaut ci-dessus ; ils sont (ré)initialisés au démarrage. |
| `Public Key Retrieval is not allowed` | Déjà géré par `allowPublicKeyRetrieval=true` dans l'URL. |
| Accents mal affichés en base | La base est en `utf8mb4` ; l'URL force `characterEncoding=UTF-8`. |

---

## 9. Livrables couverts

- ✅ Code source complet (Maven).
- ✅ Script SQL de création + initialisation.
- ✅ Modèle relationnel (tables, clés, contraintes, index).
- ✅ Guide d'utilisation (ce fichier).
- ⏳ Diagrammes UML / rapport de projet : à produire à partir de ce modèle.
