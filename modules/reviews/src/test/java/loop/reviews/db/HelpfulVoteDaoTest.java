package loop.reviews.db;

import loop.reviews.model.HelpfulVote;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HelpfulVoteDaoTest {

    private Connection connection;
    private HelpfulVoteDao dao;

    @BeforeEach
    void createDatabase() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE reviews_reviews (" +
                    "id INTEGER PRIMARY KEY, helpful_count INTEGER NOT NULL, " +
                    "unhelpful_count INTEGER NOT NULL)");
            statement.execute("CREATE TABLE reviews_helpful_votes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, review_id INTEGER NOT NULL, " +
                    "customer_id INTEGER NOT NULL, vote_type TEXT NOT NULL, created_at INTEGER NOT NULL, " +
                    "UNIQUE(review_id,customer_id))");
            statement.execute("INSERT INTO reviews_reviews VALUES(1,10,2)");
        }
        dao = new HelpfulVoteDao(connection);
    }

    @AfterEach
    void closeDatabase() throws Exception {
        connection.close();
    }

    @Test
    void firstChoiceAddsVote() {
        assertEquals(
                HelpfulVoteDao.ToggleResult.ADDED,
                HelpfulVoteDao.decideToggle(null, HelpfulVote.HELPFUL));
    }

    @Test
    void selectingSameChoiceRemovesVote() {
        assertEquals(
                HelpfulVoteDao.ToggleResult.REMOVED,
                HelpfulVoteDao.decideToggle(HelpfulVote.HELPFUL, HelpfulVote.HELPFUL));
    }

    @Test
    void selectingOtherChoiceSwitchesVote() {
        assertEquals(
                HelpfulVoteDao.ToggleResult.SWITCHED,
                HelpfulVoteDao.decideToggle(HelpfulVote.HELPFUL, HelpfulVote.UNHELPFUL));
    }

    @Test
    void rejectsUnknownVoteType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> HelpfulVoteDao.decideToggle(null, "maybe"));
    }

    @Test
    void toggleUpdatesStoredChoiceAndCounters() throws Exception {
        assertEquals(
                HelpfulVoteDao.ToggleResult.ADDED,
                dao.toggle(1, 7, HelpfulVote.HELPFUL));
        assertCounts(11, 2);

        assertEquals(
                HelpfulVoteDao.ToggleResult.SWITCHED,
                dao.toggle(1, 7, HelpfulVote.UNHELPFUL));
        assertCounts(10, 3);

        assertEquals(
                HelpfulVoteDao.ToggleResult.REMOVED,
                dao.toggle(1, 7, HelpfulVote.UNHELPFUL));
        assertCounts(10, 2);

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT COUNT(*) FROM reviews_helpful_votes")) {
            assertEquals(0, result.getInt(1));
        }
    }

    private void assertCounts(int helpful, int unhelpful) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT helpful_count,unhelpful_count FROM reviews_reviews WHERE id=1")) {
            assertEquals(helpful, result.getInt("helpful_count"));
            assertEquals(unhelpful, result.getInt("unhelpful_count"));
        }
    }
}
