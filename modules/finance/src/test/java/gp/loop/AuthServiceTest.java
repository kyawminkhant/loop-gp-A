package gp.loop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import gp.loop.db.Database;
import gp.loop.service.AuthService;

/**
 * JUnit tests for the login credential logic (component brief: secure login).
 * Runs against a throw-away SQLite database via the {@code loop.db.path} override.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthServiceTest {

    static {
        try {
            if (System.getProperty("loop.db.path") == null) {
                Path tmp = Files.createTempFile("loop-test-", ".db");
                Files.deleteIfExists(tmp);
                System.setProperty("loop.db.path", tmp.toAbsolutePath().toString());
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private final AuthService auth = new AuthService();

    @BeforeAll
    void setUp() throws Exception {
        Database.initialize();
    }

    @Test
    void passwordsAreStoredHashedNotPlainText() {
        String hash = AuthService.sha256("admin123");
        assertEquals(64, hash.length());
        assertNotEquals("admin123", hash);
    }

    @Test
    void seededAdminCanAuthenticate() throws Exception {
        assertTrue(auth.authenticate("admin@loop.co.uk", "admin123"));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        assertFalse(auth.authenticate("admin@loop.co.uk", "wrong-password"));
    }

    @Test
    void unknownEmailIsRejected() throws Exception {
        assertFalse(auth.authenticate("nobody@loop.co.uk", "admin123"));
    }

    @Test
    void newUserCanRegisterThenLogIn() throws Exception {
        assertTrue(auth.register("arta@loop.co.uk", "secret99"));
        assertTrue(auth.authenticate("arta@loop.co.uk", "secret99"));
    }

    @Test
    void duplicateEmailRegistrationIsRejected() throws Exception {
        auth.register("dupe@loop.co.uk", "first");
        assertFalse(auth.register("dupe@loop.co.uk", "second"));
        assertTrue(auth.authenticate("dupe@loop.co.uk", "first"));
    }
}
