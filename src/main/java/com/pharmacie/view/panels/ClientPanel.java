package com.pharmacie.view.panels;

import com.pharmacie.model.Client;
import com.pharmacie.model.Vente;
import com.pharmacie.service.ClientService;
import com.pharmacie.util.AppException;
import com.pharmacie.util.FormatUtil;
import com.pharmacie.util.UIHelper;
import com.pharmacie.view.components.ModulePanel;
import com.pharmacie.view.components.UIFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Module de gestion des clients : liste, recherche, CRUD, historique des achats.
 */
public class ClientPanel extends ModulePanel {

    private final ClientService service = new ClientService();

    private final JTextField champRecherche = UIFactory.champRecherche("Rechercher un client...");
    private final DefaultTableModel modele = UIFactory.modeleTable(
            "ID", "Nom", "Prenom", "Telephone", "Email");
    private final JTable table = new JTable(modele);

    private List<Client> donnees;

    public ClientPanel() {
        super("Clients", "Les clients n'ont pas de compte ; infos saisies pour la vente et le recu");

        champRecherche.addActionListener(e -> rafraichir());
        UIFactory.liveSearch(champRecherche, this::rafraichir);

        JButton btnNouveau = UIFactory.boutonPrincipal("+ Nouveau");
        btnNouveau.addActionListener(e -> ouvrirFormulaire(null));
        JButton btnModifier = UIFactory.boutonSecondaire("Modifier");
        btnModifier.addActionListener(e -> modifierSelection());
        JButton btnHistorique = UIFactory.boutonSecondaire("Historique achats");
        btnHistorique.addActionListener(e -> ouvrirHistorique());
        JButton btnSupprimer = UIFactory.boutonDanger("Supprimer");
        btnSupprimer.addActionListener(e -> supprimerSelection());

        ajouterAction(champRecherche);
        ajouterAction(btnNouveau);
        ajouterAction(btnModifier);
        ajouterAction(btnHistorique);
        ajouterAction(btnSupprimer);

        table.getColumnModel().getColumn(0).setMaxWidth(44);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) modifierSelection();
            }
        });

        setContenu(UIFactory.tableStylisee(table));
    }

    @Override
    public void rafraichir() {
        donnees = service.rechercher(champRecherche.getText().trim());
        modele.setRowCount(0);
        for (Client c : donnees) {
            modele.addRow(new Object[]{
                    c.getId(), c.getNom(),
                    c.getPrenom() == null ? "-" : c.getPrenom(),
                    c.getTelephone() == null ? "-" : c.getTelephone(),
                    c.getEmail() == null ? "-" : c.getEmail()});
        }
        setSousTitre(donnees.size() + " client(s)");
    }

    private Client selection() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) modele.getValueAt(row, 0);
        return donnees.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

    private void modifierSelection() {
        Client c = selection();
        if (c == null) {
            UIHelper.avertissement(this, "Selectionnez un client a modifier.");
            return;
        }
        ouvrirFormulaire(c);
    }

    private void supprimerSelection() {
        Client c = selection();
        if (c == null) {
            UIHelper.avertissement(this, "Selectionnez un client a supprimer.");
            return;
        }
        if (UIHelper.confirmer(this, "Supprimer le client \"" + c.getNomComplet().trim() + "\" ?")) {
            try {
                service.supprimer(c.getId());
                UIHelper.succes(this, "Client supprime.");
                rafraichir();
            } catch (AppException ex) {
                UIHelper.erreur(this, ex.getMessage());
            }
        }
    }

    private void ouvrirHistorique() {
        Client c = selection();
        if (c == null) {
            UIHelper.avertissement(this, "Selectionnez un client.");
            return;
        }
        new HistoriqueDialog(c).setVisible(true);
    }

    private void ouvrirFormulaire(Client existant) {
        new ClientDialog(existant).setVisible(true);
    }

    // ------------------------------------------------------------ DIALOGUE HISTORIQUE

    private class HistoriqueDialog extends JDialog {
        HistoriqueDialog(Client client) {
            super((Frame) SwingUtilities.getWindowAncestor(ClientPanel.this),
                    "Historique des achats - " + client.getNomComplet().trim(), true);

            DefaultTableModel m = UIFactory.modeleTable("Reference", "Date", "Montant", "Vendeur");
            JTable t = new JTable(m);
            for (Vente v : service.historiqueVentes(client.getId())) {
                m.addRow(new Object[]{
                        v.getReference(),
                        FormatUtil.dateHeure(v.getDateVente()),
                        FormatUtil.montant(v.getMontantTotal()),
                        v.getUtilisateurNom() == null ? "-" : v.getUtilisateurNom()});
            }

            JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            boutons.setBackground(Color.WHITE);
            JButton fermer = UIFactory.boutonSecondaire("Fermer");
            fermer.addActionListener(e -> dispose());
            boutons.add(fermer);

            setLayout(new BorderLayout());
            add(UIFactory.tableStylisee(t), BorderLayout.CENTER);
            add(boutons, BorderLayout.SOUTH);
            setSize(620, 400);
            setLocationRelativeTo(ClientPanel.this);
        }
    }

    // ------------------------------------------------------------ DIALOGUE

    private class ClientDialog extends JDialog {
        private final JTextField nom = new JTextField(16);
        private final JTextField prenom = new JTextField(16);
        private final JTextField telephone = new JTextField(16);
        private final JTextField email = new JTextField(16);
        private final JTextField adresse = new JTextField(16);
        private final Client client;

        ClientDialog(Client existant) {
            super((Frame) SwingUtilities.getWindowAncestor(ClientPanel.this),
                    existant == null ? "Nouveau client" : "Modifier le client", true);
            this.client = existant != null ? existant : new Client();

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(Color.WHITE);
            form.setBorder(BorderFactory.createEmptyBorder(18, 18, 8, 18));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(6, 6, 6, 6);
            g.anchor = GridBagConstraints.WEST;
            g.fill = GridBagConstraints.HORIZONTAL;

            int y = 0;
            y = ligne(form, g, y, "Nom *", nom);
            y = ligne(form, g, y, "Prenom", prenom);
            y = ligne(form, g, y, "Telephone", telephone);
            y = ligne(form, g, y, "Email", email);
            y = ligne(form, g, y, "Adresse", adresse);

            if (existant != null) {
                nom.setText(client.getNom());
                prenom.setText(client.getPrenom());
                telephone.setText(client.getTelephone());
                email.setText(client.getEmail());
                adresse.setText(client.getAdresse());
            }

            JPanel boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            boutons.setBackground(Color.WHITE);
            JButton annuler = UIFactory.boutonSecondaire("Annuler");
            annuler.addActionListener(e -> dispose());
            JButton enregistrer = UIFactory.boutonPrincipal("Enregistrer");
            enregistrer.addActionListener(e -> enregistrer());
            boutons.add(annuler);
            boutons.add(enregistrer);

            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(boutons, BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(ClientPanel.this);
        }

        private int ligne(JPanel p, GridBagConstraints g, int y, String libelle, JComponent champ) {
            g.gridx = 0; g.gridy = y; g.weightx = 0;
            p.add(UIFactory.labelChamp(libelle), g);
            g.gridx = 1; g.weightx = 1;
            p.add(champ, g);
            return y + 1;
        }

        private void enregistrer() {
            try {
                client.setNom(nom.getText().trim());
                client.setPrenom(prenom.getText().trim());
                client.setTelephone(telephone.getText().trim());
                client.setEmail(email.getText().trim());
                client.setAdresse(adresse.getText().trim());

                service.enregistrer(client);
                UIHelper.succes(this, "Client enregistre.");
                dispose();
                rafraichir();
            } catch (AppException ex) {
                UIHelper.erreur(this, ex.getMessage());
            }
        }
    }
}
