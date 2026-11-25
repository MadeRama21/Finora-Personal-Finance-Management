package com.finora.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyFormatter {

    private static final ThreadLocal<NumberFormat> FORMAT =
            ThreadLocal.withInitial(() -> NumberFormat.getCurrencyInstance(Locale.of("id", "ID")));

    private CurrencyFormatter() {
    }

    public static String format(BigDecimal amount) {
        return FORMAT.get().format(amount == null ? BigDecimal.ZERO : amount);
    }
}
