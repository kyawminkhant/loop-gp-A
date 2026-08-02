package utils;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("^[0-9]{10,15}$");

    private static final Pattern ID_CARD_PATTERN =
            Pattern.compile("^[a-zA-Z0-9 -]{4,30}$");

    private static final Pattern UNSAFE_CHARACTERS =
            Pattern.compile("[@#/\\\\?\"'~`$]");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidMobile(String mobile) {
        return mobile != null && MOBILE_PATTERN.matcher(mobile.trim()).matches();
    }

    public static boolean isValidIdCard(String idCard) {
        return idCard != null && ID_CARD_PATTERN.matcher(idCard.trim()).matches();
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean containsUnsafeCharacters(String value) {
        return value != null && UNSAFE_CHARACTERS.matcher(value).find();
    }
}