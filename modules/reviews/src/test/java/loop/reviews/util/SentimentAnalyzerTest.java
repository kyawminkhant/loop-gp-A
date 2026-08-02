package loop.reviews.util;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for keyword-based sentiment analysis (FR9). */
class SentimentAnalyzerTest {

    @Test
    void positiveCommentIsPositive() {
        assertEquals(SentimentAnalyzer.Sentiment.POSITIVE,
                SentimentAnalyzer.analyse("Absolutely delicious and fresh, loved it"));
    }

    @Test
    void negativeCommentIsNegative() {
        assertEquals(SentimentAnalyzer.Sentiment.NEGATIVE,
                SentimentAnalyzer.analyse("Cold, bland and disappointing"));
    }

    @Test
    void neutralCommentIsNeutral() {
        assertEquals(SentimentAnalyzer.Sentiment.NEUTRAL,
                SentimentAnalyzer.analyse("It arrived at noon"));
    }

    @Test
    void summaryReflectsMajorityAndTotalsAboutHundred() {
        int[] s = SentimentAnalyzer.summarise(Arrays.asList(
                "delicious and fresh", "loved it, so tasty", "cold and bland"));
        assertTrue(s[0] >= s[2], "positive share should be at least the negative share");
        int sum = s[0] + s[1] + s[2];
        assertTrue(sum >= 99 && sum <= 101, "percentages should total ~100, got " + sum);
    }
}
