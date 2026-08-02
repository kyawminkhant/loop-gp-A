package loop.reviews.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lightweight keyword-based sentiment analysis (optional enhancement in the
 * Assessment A brief). Classifies a comment as positive / neutral / negative and
 * can summarise a collection of comments into percentages for the analytics panel.
 */
public final class SentimentAnalyzer {

    private static final Set<String> POSITIVE = new HashSet<>(Arrays.asList(
        "good","great","delicious","tasty","love","loved","lovely","perfect","perfectly",
        "fresh","amazing","excellent","best","nice","filling","hot","recommend","wonderful",
        "fantastic","yummy","enjoyed","favourite","favorite","sweet","creamy"));

    private static final Set<String> NEGATIVE = new HashSet<>(Arrays.asList(
        "bad","poor","cold","overcooked","undercooked","bland","terrible","awful","worst",
        "disappointing","disappointed","soggy","stale","slow","late","small","dry","rude",
        "horrible","gross","expensive","mediocre","not"));

    private SentimentAnalyzer() { }

    public enum Sentiment { POSITIVE, NEUTRAL, NEGATIVE }

    public static Sentiment analyse(String text) {
        if (text == null) return Sentiment.NEUTRAL;
        int score = 0;
        for (String raw : text.toLowerCase().split("[^a-z]+")) {
            if (raw.isEmpty()) continue;
            if (POSITIVE.contains(raw)) score++;
            if (NEGATIVE.contains(raw)) score--;
        }
        if (score > 0) return Sentiment.POSITIVE;
        if (score < 0) return Sentiment.NEGATIVE;
        return Sentiment.NEUTRAL;
    }

    /** @return int[3] = {positive%, neutral%, negative%} rounded to whole numbers. */
    public static int[] summarise(List<String> comments) {
        if (comments == null || comments.isEmpty()) return new int[]{0, 0, 0};
        int pos = 0, neu = 0, neg = 0;
        for (String c : comments) {
            switch (analyse(c)) {
                case POSITIVE: pos++; break;
                case NEGATIVE: neg++; break;
                default: neu++;
            }
        }
        int total = comments.size();
        return new int[]{
            Math.round(pos * 100f / total),
            Math.round(neu * 100f / total),
            Math.round(neg * 100f / total)
        };
    }
}
