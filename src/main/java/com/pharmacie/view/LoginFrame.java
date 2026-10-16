package com.pharmacie.view;

import com.pharmacie.model.Utilisateur;
import com.pharmacie.service.AuthService;
import com.pharmacie.util.AppException;
import com.pharmacie.util.UIHelper;
import com.pharmacie.util.UITheme;
import com.pharmacie.view.components.UIFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Fenetre de connexion a l'application.
 */
public class LoginFrame extends JFrame {

    private final AuthService authService = new AuthService();
    private final JTextField champLogin = new JTextField(18);
    private final JPasswordField champMdp = new JPasswordField(18);

    public LoginFrame() {
        setTitle("Pharmacie - Connexion");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(860, 520);
        setMinimumSize(new Dimension(760, 480));
        setLocationRelativeTo(null);
        setIconImages(com.pharmacie.view.components.IconFactory.appIcons());
        setContentPane(construire());
        getRootPane().setDefaultButton(null);
    }

    private JPanel construire() {
        JPanel root = new JPanel(new GridLayout(1, 2));

        // ---- Cote gauche : banniere ----
        JPanel gauche = new JPanel(new GridBagLayout());
        gauche.setBackground(UITheme.PRIMARY);
        JPanel marque = new JPanel();
        marque.setOpaque(false);
        marque.setLayout(new BoxLayout(marque, BoxLayout.Y_AXIS));
        JLabel logo = new JLabel(com.pharmacie.view.components.IconFactory.icon("pharmacy", 96, Color.WHITE));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel nom = new JLabel("PharmaGest");
        nom.setFont(new Font("Segoe UI", Font.BOLD, 34));
        nom.setForeground(Color.WHITE);
        nom.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel slogan = new JLabel("Gestion de pharmacie");
        slogan.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        slogan.setForeground(new Color(0xE0F2F1));
        slogan.setAlignmentX(Component.CENTER_ALIGNMENT);
        marque.add(logo);
        marque.add(Box.createVerticalStrut(10));
        marque.add(nom);
        marque.add(Box.createVerticalStrut(6));
        marque.add(slogan);
        gauche.add(marque);

        // ---- Cote droit : formulaire ----
        JPanel droite = new JPanel(new GridBagLayout());
        droite.setBackground(Color.WHITE);
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel titre = new JLabel("Connexion");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titre.setForeground(UITheme.TEXT);
        titre.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sous = new JLabel("Accedez a votre espace de travail");
        sous.setFont(UITheme.FONT_NORMAL);
        sous.setForeground(UITheme.TEXT_MUTED);
        sous.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblLogin = UIFactory.labelChamp("Identifiant");
        lblLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        champLogin.setFont(UITheme.FONT_NORMAL);
        champLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        champLogin.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblMdp = UIFactory.labelChamp("Mot de passe");
        lblMdp.setAlignmentX(Component.LEFT_ALIGNMENT);
        champMdp.setFont(UITheme.FONT_NORMAL);
        champMdp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        champMdp.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Bouton "oeil" pour afficher / masquer le mot de passe
        UIFactory.showPasswordToggle(champMdp);

        JButton btnConnexion = UIFactory.boutonPrincipal("Se connecter");
        btnConnexion.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnConnexion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnConnexion.addActionListener(this::connecter);

        KeyAdapter entree = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) connecter(null);
            }
        };
        champLogin.addKeyListener(entree);
        champMdp.addKeyListener(entree);

        form.add(titre);
        form.add(Box.createVerticalStrut(4));
        form.add(sous);
        form.add(Box.createVerticalStrut(24));
        form.add(lblLogin);
        form.add(Box.createVerticalStrut(6));
        form.add(champLogin);
        form.add(Box.createVerticalStrut(16));
        form.add(lblMdp);
        form.add(Box.createVerticalStrut(6));
        form.add(champMdp);
        form.add(Box.createVerticalStrut(24));
        form.add(btnConnexion);

        form.setPreferredSize(new Dimension(320, 420));
        droite.add(form);

        root.add(gauche);
        root.add(droite);
        return root;
    }

    private void connecter(ActionEvent e) {
        String login = champLogin.getText();
        String mdp = new String(champMdp.getPassword());
        try {
            Utilisateur u = authService.connexion(login, mdp);
            dispose();
            SwingUtilities.invokeLater(() -> new MainFrame(u).setVisible(true));
        } catch (AppException ex) {
            UIHelper.erreur(this, ex.getMessage());
            champMdp.setText("");
        }
    }
}
