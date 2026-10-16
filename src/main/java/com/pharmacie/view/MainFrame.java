package com.pharmacie.view;

import com.pharmacie.model.Utilisateur;
import com.pharmacie.util.Session;
import com.pharmacie.util.UIHelper;
import com.pharmacie.util.UITheme;
import com.pharmacie.view.components.IconFactory;
import com.pharmacie.view.components.ModulePanel;
import com.pharmacie.view.panels.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fenetre principale : barre laterale de navigation + zone de contenu (CardLayout).
 * La navigation respecte les droits d'acces selon le role.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contenu = new JPanel(cardLayout);
    private final Map<String, ModulePanel> panneaux = new LinkedHashMap<>();
    private final List<JButton> boutonsNav = new ArrayList<>();
    private final JLabel titreEntete = new JLabel();

    public MainFrame(Utilisateur utilisateur) {
        setTitle("PharmaGest - Gestion de pharmacie");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1080, 660));
        setLocationRelativeTo(null);
        setIconImages(IconFactory.appIcons());

        contenu.setBackground(UITheme.BG);

        add(construireSidebar(), BorderLayout.WEST);
        add(construireContenuAvecEntete(), BorderLayout.CENTER);

        enregistrerPanneaux();
        if (!panneaux.isEmpty()) {
            afficher(panneaux.keySet().iterator().next());
        }
    }

    // ----------------------------------------------------------------- SIDEBAR

    private JPanel construireSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(UITheme.SIDEBAR_BG);
        side.setPreferredSize(new Dimension(248, 0));

        // En-tete de la marque
        JPanel marque = new JPanel();
        marque.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        marque.setBackground(UITheme.PRIMARY_DARK);
        marque.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 20));
        JLabel logo = new JLabel("PharmaGest");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(Color.WHITE);
        logo.setIcon(IconFactory.icon("pharmacy", 26, Color.WHITE));
        logo.setIconTextGap(10);
        marque.add(logo);
        side.add(marque);
        side.add(Box.createVerticalStrut(10));

        // Les boutons de navigation seront ajoutes par enregistrerPanneaux()
        navContainer = new JPanel();
        navContainer.setLayout(new BoxLayout(navContainer, BoxLayout.Y_AXIS));
        navContainer.setBackground(UITheme.SIDEBAR_BG);
        navContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(navContainer);

        side.add(Box.createVerticalGlue());

        // Pied : utilisateur + deconnexion
        JPanel pied = new JPanel();
        pied.setLayout(new BoxLayout(pied, BoxLayout.Y_AXIS));
        pied.setBackground(UITheme.SIDEBAR_BG);
        pied.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 16));
        pied.setAlignmentX(Component.LEFT_ALIGNMENT);

        Utilisateur u = Session.getUtilisateur();
        JLabel nomU = new JLabel(u.getNomComplet());
        nomU.setFont(UITheme.FONT_BOLD);
        nomU.setForeground(UITheme.SIDEBAR_TEXT);
        JLabel roleU = new JLabel(u.getRole().getLibelle());
        roleU.setFont(UITheme.FONT_SMALL);
        roleU.setForeground(UITheme.PRIMARY_LIGHT);

        JButton btnDeco = new JButton("Deconnexion");
        btnDeco.setFocusPainted(false);
        btnDeco.setForeground(Color.WHITE);
        btnDeco.setBackground(UITheme.DANGER);
        btnDeco.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btnDeco.setIcon(IconFactory.icon("logout", 16, Color.WHITE));
        btnDeco.setIconTextGap(8);
        btnDeco.setHorizontalAlignment(SwingConstants.LEFT);
        btnDeco.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDeco.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnDeco.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnDeco.addActionListener(e -> deconnexion());

        pied.add(nomU);
        pied.add(Box.createVerticalStrut(2));
        pied.add(roleU);
        pied.add(Box.createVerticalStrut(12));
        pied.add(btnDeco);
        side.add(pied);

        return side;
    }

    private JPanel navContainer;

    private static String navIcon(String cle) {
        return switch (cle) {
            case "dashboard" -> "dashboard";
            case "caisse" -> "cart";
            case "clients" -> "client";
            case "historique" -> "history";
            case "stock" -> "stock";
            case "medicaments" -> "medication";
            case "categories" -> "category";
            case "fournisseurs" -> "supplier";
            case "appro" -> "appro";
            case "rapports" -> "report";
            case "utilisateurs" -> "users";
            default -> "dashboard";
        };
    }

    private JButton creerBoutonNav(String cle, String libelle) {
        JButton b = new JButton(libelle);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(UITheme.FONT_NORMAL);
        b.setForeground(UITheme.SIDEBAR_TEXT);
        b.setBackground(UITheme.SIDEBAR_BG);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        b.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setIcon(IconFactory.icon(navIcon(cle), 18, UITheme.SIDEBAR_TEXT));
        b.setIconTextGap(14);
        b.putClientProperty("cle", cle);
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!cle.equals(cleActive)) b.setBackground(UITheme.SIDEBAR_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!cle.equals(cleActive)) b.setBackground(UITheme.SIDEBAR_BG);
            }
        });
        b.addActionListener(e -> afficher(cle));
        return b;
    }

    private String cleActive;

    // -------------------------------------------------------------- CONTENU

    private JPanel construireContenuAvecEntete() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UITheme.BG);
        wrap.add(contenu, BorderLayout.CENTER);
        return wrap;
    }

    private void enregistrerPanneaux() {
        boolean stock = Session.peutGererStock();
        boolean admin = Session.peutGererUtilisateurs();

        // Accessibles a tous
        ajouter("dashboard", "Tableau de bord", new DashboardPanel(this));
        ajouter("caisse", "Ventes / Caisse", new VentePanel());
        ajouter("clients", "Clients", new ClientPanel());
        ajouter("historique", "Historique des ventes", new HistoriqueVentePanel());
        ajouter("stock", "Stock", new StockPanel());

        // Gestion (admin + pharmacien)
        if (stock) {
            ajouter("medicaments", "Medicaments", new MedicamentPanel());
            ajouter("categories", "Categories", new CategoriePanel());
            ajouter("fournisseurs", "Fournisseurs", new FournisseurPanel());
            ajouter("appro", "Approvisionnements", new ApprovisionnementPanel());
            ajouter("rapports", "Rapports & statistiques", new RapportPanel());
        }

        // Administration (admin seulement)
        if (admin) {
            ajouter("utilisateurs", "Utilisateurs", new UtilisateurPanel());
        }
    }

    private void ajouter(String cle, String libelle, ModulePanel panel) {
        panneaux.put(cle, panel);
        contenu.add(panel, cle);
        JButton b = creerBoutonNav(cle, libelle);
        boutonsNav.add(b);
        navContainer.add(b);
        navContainer.add(Box.createVerticalStrut(2));
    }

    /** Affiche le module identifie par sa cle et rafraichit ses donnees. */
    public void afficher(String cle) {
        ModulePanel p = panneaux.get(cle);
        if (p == null) return;
        cleActive = cle;
        try {
            p.rafraichir();
        } catch (Exception ex) {
            UIHelper.erreur(this, "Erreur de chargement du module : " + ex.getMessage());
        }
        cardLayout.show(contenu, cle);
        majBoutons();
    }

    private void majBoutons() {
        for (JButton b : boutonsNav) {
            String cle = (String) b.getClientProperty("cle");
            if (cle.equals(cleActive)) {
                b.setBackground(UITheme.SIDEBAR_ACTIVE);
                b.setForeground(Color.WHITE);
                b.setFont(UITheme.FONT_BOLD);
            } else {
                b.setBackground(UITheme.SIDEBAR_BG);
                b.setForeground(UITheme.SIDEBAR_TEXT);
                b.setFont(UITheme.FONT_NORMAL);
            }
        }
    }

    private void deconnexion() {
        Session.fermer();
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
