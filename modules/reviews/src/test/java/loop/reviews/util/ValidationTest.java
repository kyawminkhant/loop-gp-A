package loop.reviews.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for the input validation rules (FR1, FR3, FR10). */
class ValidationTest {

    @Test
    void disallowedCharactersAreRejected() {
        assertTrue(Validation.hasDisallowedChars("great food @ home"));
        assertTrue(Validation.hasDisallowedChars("cost was $10"));
        assertEquals("#", Validation.firstDisallowedChar("nice #1 meal"));
    }

    @Test
    void ordinaryPunctuationIsAllowed() {
        assertFalse(Validation.hasDisallowedChars("Tasty, fresh and hot. Would order again."));
        assertNull(Validation.firstDisallowedChar("Lovely dish - well cooked."));
    }

    @Test
    void emailFormatIsValidated() {
        assertTrue(Validation.isValidEmail("tasmia@loop.com"));
        assertFalse(Validation.isValidEmail("tasmia.loop.com"));
        assertFalse(Validation.isValidEmail(""));
    }

    @Test
    void passwordMustBeAtLeastEightCharacters() {
        assertTrue(Validation.isValidPassword("password1"));
        assertFalse(Validation.isValidPassword("short"));
    }

    @Test
    void ratingBoundariesAreEnforced() {
        assertTrue(Validation.isValidRating(1));
        assertTrue(Validation.isValidRating(5));
        assertFalse(Validation.isValidRating(0));
        assertFalse(Validation.isValidRating(6));
    }
}
