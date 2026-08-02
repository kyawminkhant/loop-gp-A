package loop.reviews.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for the edit/delete time window (FR4, FR5). */
class ReviewWindowTest {

    private Review review(long createdAt, int durationSeconds) {
        Review r = new Review();
        r.setCreatedAt(createdAt);
        r.setEditDurationSeconds(durationSeconds);
        return r;
    }

    @Test
    void windowOpenForFreshReview() {
        Review r = review(System.currentTimeMillis(), 300);
        assertTrue(r.isEditWindowOpen());
        assertTrue(r.remainingSeconds() > 0 && r.remainingSeconds() <= 300);
    }

    @Test
    void windowClosedAfterDurationElapsed() {
        long tenMinutesAgo = System.currentTimeMillis() - (10L * 60 * 1000);
        Review r = review(tenMinutesAgo, 300);
        assertFalse(r.isEditWindowOpen());
        assertEquals(0, r.remainingSeconds());
    }
}
