package tests;

import org.junit.jupiter.api.Test;
import utils.PasswordUtil;
import utils.ValidationUtil;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @Test
    void validEmail_acceptsUniversityAddress() {
        assertTrue(ValidationUtil.isValidEmail("student@brunel.ac.uk"));
    }

    @Test
    void invalidEmail_rejectsMissingAtSymbol() {
        assertFalse(ValidationUtil.isValidEmail("not-an-email"));
    }

    @Test
    void validMobile_acceptsTenDigits() {
        assertTrue(ValidationUtil.isValidMobile("0712345678"));
    }

    @Test
    void invalidMobile_rejectsShortNumber() {
        assertFalse(ValidationUtil.isValidMobile("12345"));
    }

    @Test
    void unsafeCharacters_detectedInText() {
        assertTrue(ValidationUtil.containsUnsafeCharacters("hello@world"));
    }

    @Test
    void passwordHash_doesNotStorePlainText() {
        String hash = PasswordUtil.hash("secret123");
        assertNotEquals("secret123", hash);
        assertTrue(PasswordUtil.matches("secret123", hash));
    }
}
