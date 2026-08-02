package loop.reviews.util;

import java.util.regex.Pattern;

/**
 * Central validation rules for the Reviews & Ratings component.
 *
 * Disallowed characters (FR3 / FR10): @ # / \ ? " ' ~ ` $
 * The Assessment A brief lists "@,.#/\?"'~`$"; the commas and the full stop in
 * that list are read as list separators, since blocking '.' and ',' would make
 * ordinary review sentences impossible. This is stated as an assumption in the
 * accompanying notes.
 */
public final class Validation {

    // Matches any single disallowed character.
    private static final Pattern DISALLOWED =
        Pattern.compile("[@#/\\\\?\"'~`$]");

    private static final Pattern EMAIL =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private Validation() { }

    /** @return the first disallowed character found, or null if the text is clean. */
    public static String firstDisallowedChar(String text) {
        if (text == null) return null;
        java.util.regex.Matcher m = DISALLOWED.matcher(text);
        return m.find() ? m.group() : null;
    }

    public static boolean hasDisallowedChars(String text) {
        return firstDisallowedChar(text) != null;
    }

    public static boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
