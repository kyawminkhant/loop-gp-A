package loop.reviews.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/** Lightweight local checks used to keep clearly inappropriate reviews out of customer views. */
public final class ContentModeration {

    private static final Map<String, Pattern> RULES = new LinkedHashMap<>();

    static {
        RULES.put(
                "Offensive or abusive language",
                Pattern.compile("\\b(fuck(?:ed|ing)?|shit(?:ty)?|bitch(?:es)?|asshole|bastard|cunt)\\b",
                        Pattern.CASE_INSENSITIVE));
        RULES.put(
                "Harassment or threatening language",
                Pattern.compile("\\b(kill yourself|go die|hope you die|you should die|i(?:'|’)?ll kill you)\\b",
                        Pattern.CASE_INSENSITIVE));
        RULES.put(
                "Spam or promotional content",
                Pattern.compile("https?://|www\\.|\\b(click here|buy followers|free money|promo code)\\b",
                        Pattern.CASE_INSENSITIVE));
        RULES.put(
                "Personal contact information",
                Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}|(?:\\+?\\d[ -]?){8,}",
                        Pattern.CASE_INSENSITIVE));
    }

    private ContentModeration() { }

    /** @return a moderation reason, or {@code null} when no rule matches. */
    public static String flagReason(String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            return null;
        }
        for (Map.Entry<String, Pattern> rule : RULES.entrySet()) {
            if (rule.getValue().matcher(comment).find()) {
                return rule.getKey();
            }
        }
        return null;
    }

    public static boolean shouldHide(String comment) {
        return flagReason(comment) != null;
    }
}
