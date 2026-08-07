package loop.reviews.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentModerationTest {

    @Test
    void allowsNormalPositiveAndNegativeFoodFeedback() {
        assertNull(ContentModeration.flagReason(
                "The meal was disappointing and arrived cold, but the packaging was neat."));
        assertFalse(ContentModeration.shouldHide(
                "Excellent flavour and a generous portion. I would order this again."));
    }

    @Test
    void flagsAbusiveOrThreateningContent() {
        assertTrue(ContentModeration.shouldHide("This food is shit and the chef is an asshole."));
        assertNotNull(ContentModeration.flagReason("You should die for serving this."));
    }

    @Test
    void flagsSpamAndPersonalContactDetails() {
        assertTrue(ContentModeration.shouldHide("Click here for free money at https://example.com"));
        assertTrue(ContentModeration.shouldHide("Contact me on 07700 900123 for a refund."));
    }
}
