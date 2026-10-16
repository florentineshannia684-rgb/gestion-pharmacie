package com.pharmacie.view.components;

import com.pharmacie.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Rendu de cellule avec lignes alternees et marge interne, pour un rendu
 * moderne et lisible des tableaux.
 */
public class StripedCellRenderer extends DefaultTableCellRenderer {

    private static final Color STRIPE = new Color(0xF7F9FA);

    public StripedCellRenderer() {
        setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (isSelected) {
            setBackground(UITheme.PRIMARY_LIGHT);
            setForeground(Color.WHITE);
        } else {
            setBackground(row % 2 == 0 ? Color.WHITE : STRIPE);
            setForeground(UITheme.TEXT);
        }
        setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        return this;
    }
}
