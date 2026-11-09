package io.github.mugishajc.tasks.api;

import io.github.mugishajc.tasks.service.InvalidQueryException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Locale;

/**
 * Lenient parsing for query parameters. Doing it by hand gives a clear 400
 * instead of the 404 JAX-RS returns when a query parameter fails to convert.
 */
final class QueryParams {

    private QueryParams() {
    }

    static <E extends Enum<E>> E parseEnum(Class<E> type, String name, String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        try {
            return Enum.valueOf(type, normalized);
        } catch (IllegalArgumentException e) {
            throw new InvalidQueryException("Invalid " + name + " '" + raw + "'. Allowed: "
                    + Arrays.toString(type.getEnumConstants()));
        }
    }

    static LocalDate parseDate(String name, String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim());
        } catch (DateTimeParseException e) {
            throw new InvalidQueryException("Invalid " + name + " '" + raw + "', expected yyyy-MM-dd");
        }
    }
}
