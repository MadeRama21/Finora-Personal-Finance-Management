package com.finora.util;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public final class MonthNames {

    private static final Locale INDONESIAN = Locale.of("id", "ID");

    private MonthNames() {
    }

    public static String full(int month) {
        return Month.of(month).getDisplayName(TextStyle.FULL, INDONESIAN);
    }
}