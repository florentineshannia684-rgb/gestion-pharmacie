package com.pharmacie.view.components;

import com.pharmacie.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.function.Function;

/**
 * Rendu de cellule sous forme de "pastille" (badge) coloree, pour les colonnes
 * de statut (ex. STOCK FAIBLE, EXPIRE, OK). Conserve les lignes alternees pour
 * le fond de la cellule.
 */
public class BadgeRenderer extends DefaultTableCellRenderer {

    private static final Color STRIPE = new Color(0xF7F9FA);

    private final Function<String, Color> couleurPour;
    private Color badge;
    private boolean selected;
    private int rowIndex;

    public BadgeRenderer() {
        this(BadgeRenderer::couleurStatutParDefaut);
    }

    public BadgeRenderer(Function<String, Color> couleurPour) {
        this.couleurPour = couleurPour;
        setHorizontalAlignment(CENTER);
        setFont(UITheme.FONT_SMALL);
    }

    /** Mapping par defaut des libelles de statut vers une couleur. */
    public static Color couleurStatutParDefaut(String v) {
        if (v == null) return null;
        String s = v.toUpperCase();
        if (s.contains("EXPIRE") || s.contains("RUPTURE") || s.contains("FAIBLE")) return UITheme.DANGER;
        if (s.contains("PROCHE") || s.contains("BIENTOT") || s.contains("ALERTE")) return UITheme.WARNING;
        if (s.contains("OK") || s.contains("NORMAL") || s.contains("BON") || s.contains("DISPONIBLE")) return UITheme.SUCCESS;
        return null;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String v = value == null ? "" : value.toString();
        this.badge = couleurPour.apply(v);
        this.selected = isSelected;
        this.rowIndex = row;
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        if (badge != null) {
            setForeground(Color.WHITE);
        } else {
            setForeground(isSelected ? Color.WHITE : UITheme.TEXT);
        }
        setOpaque(false);
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fond de la cellule (selection ou ligne alternee)
        Color fond = selected ? UITheme.PRIMARY_LIGHT : (rowIndex % 2 == 0 ? Color.WHITE : STRIPE);
        g2.setColor(fond);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Pastille coloree
        if (badge != null) {
            FontMetrics fm = g2.getFontMetrics(getFont());
            String txt = getText();
            int tw = fm.stringWidth(txt);
            int pad = 12;
            int bw = Math.min(getWidth() - 12, tw + pad * 2);
            int bh = Math.min(getHeight() - 10, 22);
            int bx = (getWidth() - bw) / 2;
            int by = (getHeight() - bh) / 2;
            g2.setColor(badge);
            g2.fillRoundRect(bx, by, bw, bh, bh, bh);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
