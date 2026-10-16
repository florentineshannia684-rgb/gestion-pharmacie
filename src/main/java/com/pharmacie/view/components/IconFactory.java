package com.pharmacie.view.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.pharmacie.util.UITheme;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Fournit les icones vectorielles (SVG) de l'application, avec recoloration
 * dynamique, et genere l'icone de l'application (barre des taches / fenetre).
 */
public final class IconFactory {

    private static final Map<String, FlatSVGIcon> cache = new HashMap<>();

    private IconFactory() {
    }

    /** Icone SVG recoloree a la couleur demandee. */
    public static FlatSVGIcon icon(String nom, int taille, Color couleur) {
        FlatSVGIcon base = cache.computeIfAbsent(nom, n -> new FlatSVGIcon("icons/" + n + ".svg"));
        FlatSVGIcon ic = base.derive(taille, taille);
        if (couleur != null) {
            ic.setColorFilter(new FlatSVGIcon.ColorFilter(c -> couleur));
        }
        return ic;
    }

    /** Icone SVG a sa couleur d'origine (teintee en gris fonce par defaut). */
    public static FlatSVGIcon icon(String nom, int taille) {
        return icon(nom, taille, UITheme.TEXT);
    }

    /**
     * Genere l'icone de l'application : carre arrondi teal avec une croix blanche.
     */
    public static Image appIcon(int taille) {
        BufferedImage img = new BufferedImage(taille, taille, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = taille / 4;
        g.setColor(UITheme.PRIMARY);
        g.fillRoundRect(0, 0, taille, taille, arc, arc);
        g.setColor(Color.WHITE);
        int th = Math.max(2, taille / 6);        // epaisseur de la croix
        int m = taille / 4;                      // marge
        g.fillRoundRect((taille - th) / 2, m, th, taille - 2 * m, th / 2, th / 2);
        g.fillRoundRect(m, (taille - th) / 2, taille - 2 * m, th, th / 2, th / 2);
        g.dispose();
        return img;
    }

    /** Liste d'icones de l'application en plusieurs tailles (pour la fenetre). */
    public static java.util.List<Image> appIcons() {
        java.util.List<Image> list = new java.util.ArrayList<>();
        for (int s : new int[]{16, 24, 32, 48, 64, 128}) {
            list.add(appIcon(s));
        }
        return list;
    }
}
