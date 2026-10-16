package com.pharmacie.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Formatage des montants et dates pour l'affichage.
 */
public final class FormatUtil {

    public static final String DEVISE = "Ar";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private FormatUtil() {
    }

    public static String montant(BigDecimal m) {
        if (m == null) m = BigDecimal.ZERO;
        return String.format("%,.2f %s", m, DEVISE);
    }

    public static String date(LocalDate d) {
        return d == null ? "" : d.format(DATE);
    }

    public static String date(LocalDateTime d) {
        return d == null ? "" : d.format(DATE);
    }

    public static String dateHeure(LocalDateTime d) {
        return d == null ? "" : d.format(DATE_HEURE);
    }
}
