package gp.loop.model;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/** One row from {@code Orders} for the finance table (JavaBean names for {@code TableView}). */
public final class OrderRow {

    private static final DateTimeFormatter HUMAN_DATE_TIME = DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a", Locale.US);
    private static final DateTimeFormatter HUMAN_DATE = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US);

    /** SQLite / JDBC often returns this shape without a {@code T}. */
    private static final DateTimeFormatter SQLITE_SPACE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final long orderId;
    private final String orderDate;
    private final double totalAmount;
    private final double cost;
    private final NumberFormat money = NumberFormat.getCurrencyInstance(Locale.US);

    public OrderRow(long orderId, String orderDate, double totalAmount, double cost) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.cost = cost;
    }

    public long getOrderId() {
        return orderId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    /**
     * Parsed from stored {@link #orderDate} strings (ISO, SQLite {@code yyyy-MM-dd HH:mm:ss}, date-only)
     * for display in the finance table.
     */
    public String getOrderDateDisplay() {
        if (orderDate == null || orderDate.isBlank()) {
            return "—";
        }
        String s = orderDate.trim();
        LocalDateTime dt = tryParseDateTime(s);
        if (dt != null) {
            return HUMAN_DATE_TIME.format(dt);
        }
        try {
            LocalDate d = LocalDate.parse(s);
            return HUMAN_DATE.format(d);
        } catch (DateTimeParseException ignored) {
            return s;
        }
    }

    /** Parsed calendar date for range filters; empty when the stored value cannot be parsed. */
    public Optional<LocalDate> getOrderDateLocal() {
        if (orderDate == null || orderDate.isBlank()) {
            return Optional.empty();
        }
        String s = orderDate.trim();
        LocalDateTime dt = tryParseDateTime(s);
        if (dt != null) {
            return Optional.of(dt.toLocalDate());
        }
        try {
            return Optional.of(LocalDate.parse(s));
        } catch (DateTimeParseException ignored) {
            return Optional.empty();
        }
    }

    private static LocalDateTime tryParseDateTime(String s) {
        try {
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException ignored) {
            // continue
        }
        try {
            return LocalDateTime.parse(s.replace(' ', 'T'));
        } catch (DateTimeParseException ignored) {
            // continue
        }
        try {
            return LocalDateTime.parse(s, SQLITE_SPACE);
        } catch (DateTimeParseException ignored) {
            // continue
        }
        return null;
    }

    public String getTotalDisplay() {
        return money.format(totalAmount);
    }

    public String getCostDisplay() {
        return money.format(cost);
    }

    public String getProfitDisplay() {
        return money.format(totalAmount - cost);
    }
}
