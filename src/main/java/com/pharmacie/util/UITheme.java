package com.pharmacie.util;

import java.awt.Color;
import java.awt.Font;

/**
 * Palette de couleurs et polices de l'application.
 *
 * Direction : vert emeraude sobre.
 *  - un accent unique (emeraude) pour la marque et les actions ;
 *  - des gris ardoise neutres pour les surfaces et le texte ;
 *  - la couleur vive reservee au sens (alerte / succes / avertissement).
 */
public final class UITheme {

    private UITheme() {
    }

    // --- Accent de marque (emeraude profond) ---
    public static final Color PRIMARY       = new Color(0x1B7A5A);
    public static final Color PRIMARY_DARK  = new Color(0x145C44);
    public static final Color PRIMARY_LIGHT = new Color(0x3E9B78); // selection / survol
    public static final Color ACCENT        = new Color(0x1B7A5A);

    // --- Barre laterale (ardoise) ---
    public static final Color SIDEBAR_BG     = new Color(0x1E293B); // slate 800
    public static final Color SIDEBAR_HOVER  = new Color(0x334155); // slate 700
    public static final Color SIDEBAR_ACTIVE = new Color(0x1B7A5A); // emeraude
    public static final Color SIDEBAR_TEXT   = new Color(0xE2E8F0); // slate 200

    // --- Surfaces & texte (neutres) ---
    public static final Color BG         = new Color(0xF8FAFC); // slate 50
    public static final Color CARD_BG    = Color.WHITE;
    public static final Color BORDER     = new Color(0xE2E8F0); // slate 200
    public static final Color TEXT       = new Color(0x1E293B); // slate 800
    public static final Color TEXT_MUTED = new Color(0x64748B); // slate 500

    // --- Couleurs semantiques (reservees au sens) ---
    public static final Color SUCCESS = new Color(0x15803D); // vert
    public static final Color WARNING = new Color(0xD97706); // ambre
    public static final Color DANGER  = new Color(0xDC2626); // rouge
    public static final Color INFO    = new Color(0x0F6E8C); // bleu-canard

    // --- Couleurs des cartes du tableau de bord ---
    // Volontairement sobres : accent de marque partout, couleur vive uniquement
    // pour les indicateurs d'alerte (stock faible / expirations).
    public static final Color CARD_BLUE   = PRIMARY;  // indicateurs neutres
    public static final Color CARD_GREEN  = PRIMARY;
    public static final Color CARD_PURPLE = PRIMARY;
    public static final Color CARD_ORANGE = WARNING;  // alerte stock faible
    public static final Color CARD_RED    = DANGER;   // alerte expirations

    // --- Polices ---
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_NORMAL   = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BOLD     = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_STAT     = new Font("Segoe UI", Font.BOLD, 26);
}
