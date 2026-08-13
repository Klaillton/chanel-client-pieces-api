package com.channel.pieces.domains;

import java.util.Locale;

public enum PieceType {

    WATCH,
    JEWELRY;

    public static PieceType fromCsv(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Type cannot be null or blank");
        }
        String normalizedValue = rawValue.trim().toUpperCase(Locale.ROOT)
                .replace("JEWELLERY", "JEWELRY");
        return PieceType.valueOf(normalizedValue);
    }

}
