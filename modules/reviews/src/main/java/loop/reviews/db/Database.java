package loop.reviews.db;

import loop.reviews.util.ContentModeration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Owns the SQLite connection and schema for the Reviews & Ratings component.
 *
 *  - Creates every table with CREATE TABLE IF NOT EXISTS on first run.
 *  - Prints the ABSOLUTE path to loop.db on startup so it can be opened in
 *    "DB Browser for SQLite".
 *  - Seeds a small amount of realistic sample data the first time it runs, so
 *    screens are populated on first launch.
 *
 * A single shared Connection is used (the app is single-threaded on the JavaFX
 * thread). SQLite foreign-key enforcement is turned on.
 */
public final class Database {

    /** The database file is created in the current working directory. */
    private static final String DB_FILE = System.getProperty("loop.db.path", "database/loop.db");
    private static final int TARGET_REVIEWS_PER_PRODUCT = 100;
    private static final String[] FAN_FAVOURITE_REVIEWERS = {
        "Naruto Uzumaki", "Monkey D. Luffy", "Satoru Gojo", "Levi Ackerman",
        "Mikasa Ackerman", "Tanjiro Kamado", "Nezuko Kamado", "Izuku Midoriya",
        "Katsuki Bakugo", "Shoto Todoroki", "Edward Elric", "Alphonse Elric",
        "Usagi Tsukino", "Son Goku", "Vegeta Briefs", "Kakashi Hatake",
        "Itachi Uchiha", "Sasuke Uchiha", "Hinata Hyuga", "Killua Zoldyck",
        "Gon Freecss", "Kurapika Kurta", "Rintaro Okabe", "Spike Spiegel",
        "Violet Evergarden", "Light Yagami", "L Lawliet", "Eren Yeager",
        "Anya Forger", "Loid Forger", "Frieren Himmel", "Maomao Jinshi",
        "Nami Bellmere", "Roronoa Zoro", "Sanji Vinsmoke", "Denji Hayakawa",
        "Power Nyako", "Jotaro Kujo", "Dio Brando", "Shinji Ikari"
    };
    private static final String[] REVIEWER_FIRST_NAMES = {
        "Aiko", "Haru", "Mei", "Ren", "Yuna", "Kaito", "Sora", "Emi", "Riku", "Akari"
    };
    private static final String[] REVIEWER_LAST_NAMES = {
        "Tanaka", "Nakamura", "Kobayashi", "Watanabe", "Ito",
        "Yamamoto", "Kato", "Yoshida", "Yamada", "Sasaki"
    };

    private static Database instance;
    private Connection connection;
    private boolean initialized;

    private Database() { }

    public static synchronized Database get() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    /** Open the connection, create tables, print path, seed data. */
    public synchronized void init() {
        try {
            if (initialized && connection != null && !connection.isClosed()) {
                return;
            }
        } catch (SQLException ignored) {
            initialized = false;
        }

        File dbFile = new File(DB_FILE).getAbsoluteFile();
        System.out.println("========================================================");
        System.out.println(" LOOP reviews_reviews - SQLite database file:");
        System.out.println("   " + dbFile.getAbsolutePath());
        System.out.println(" Open this file in 'DB Browser for SQLite' to inspect it.");
        System.out.println("========================================================");

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON;");
            }
            createTables();
            seedIfEmpty();
            seedReviewUsers();
            seedCatalogueReviews();
            flagDetectedReviews();
            new ProductDao().recalculateAllAverages();
            initialized = true;
        } catch (SQLException e) {
            initialized = false;
            throw new RuntimeException("Failed to initialise database", e);
        }
    }

    public Connection getConnection() {
        if (connection == null) {
            throw new IllegalStateException("Database.init() has not been called.");
        }
        return connection;
    }

    private void createTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute(
                "CREATE TABLE IF NOT EXISTS product_Chef (" +
                "  chefID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  chefName TEXT NOT NULL," +
                "  \"chefRating&ReviewID\" INTEGER NOT NULL DEFAULT 0," +
                "  chefDescription TEXT NOT NULL DEFAULT ''," +
                "  chefTag1 TEXT NOT NULL DEFAULT '', chefTag2 TEXT NOT NULL DEFAULT ''," +
                "  chefTag3 TEXT NOT NULL DEFAULT '', chefImage TEXT NOT NULL DEFAULT ''," +
                "  chefEmail TEXT NOT NULL DEFAULT '', chefTel TEXT NOT NULL DEFAULT ''" +
                ");");

            st.execute(
                "CREATE TABLE IF NOT EXISTS product_Products (" +
                "  productID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  productName TEXT NOT NULL, shortDescription TEXT NOT NULL," +
                "  extendedDescription TEXT NOT NULL, cost REAL NOT NULL, price REAL NOT NULL," +
                "  status INTEGER NOT NULL DEFAULT 1, spiceLevel INTEGER NOT NULL DEFAULT 0," +
                "  country TEXT NOT NULL, createdDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  updatedDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, chefID INTEGER NOT NULL," +
                "  sourceModule TEXT NOT NULL DEFAULT 'product'," +
                "  stockQuantity INTEGER NOT NULL DEFAULT 100," +
                "  FOREIGN KEY (chefID) REFERENCES product_Chef(chefID)" +
                ");");

            st.execute(
                "CREATE TABLE IF NOT EXISTS product_Category (" +
                "  categoryID INTEGER PRIMARY KEY AUTOINCREMENT, chosenSortBy TEXT NOT NULL DEFAULT ''," +
                "  chosenDietary TEXT NOT NULL, chosenHealthGoal TEXT NOT NULL," +
                "  chosenCuisines TEXT NOT NULL, productID INTEGER NOT NULL," +
                "  FOREIGN KEY (productID) REFERENCES product_Products(productID) ON DELETE CASCADE" +
                ");");

            st.execute(
                "CREATE TABLE IF NOT EXISTS product_Ratings (" +
                "  rateID INTEGER PRIMARY KEY AUTOINCREMENT, rating REAL NOT NULL DEFAULT 0," +
                "  noPeople INTEGER NOT NULL DEFAULT 0, productID INTEGER NOT NULL," +
                "  FOREIGN KEY (productID) REFERENCES product_Products(productID) ON DELETE CASCADE" +
                ");");

            st.execute(
                "CREATE TABLE IF NOT EXISTS reviews_users (" +
                "  id       INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name     TEXT    NOT NULL," +
                "  email    TEXT    NOT NULL UNIQUE," +          // unique emails
                "  password TEXT    NOT NULL," +
                "  role     TEXT    NOT NULL CHECK(role IN ('CUSTOMER','ADMIN'))," +
                "  address  TEXT" +
                ");");

            st.execute(
                "CREATE TABLE IF NOT EXISTS reviews_orders (" +
                "  id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  customer_id INTEGER NOT NULL REFERENCES reviews_users(id)," +
                "  product_id  INTEGER NOT NULL REFERENCES product_Products(productID)," +
                "  order_date  TEXT    NOT NULL" +
                ");");

            st.execute(
                "CREATE TABLE IF NOT EXISTS reviews_reviews (" +
                "  id                    INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  product_id            INTEGER NOT NULL REFERENCES product_Products(productID)," +
                "  customer_id           INTEGER NOT NULL REFERENCES reviews_users(id)," +
                "  rating                INTEGER NOT NULL CHECK(rating BETWEEN 1 AND 5)," +
                "  comment_text          TEXT    NOT NULL," +
                "  image_url             TEXT," +
                "  created_at            INTEGER NOT NULL," +          // epoch millis
                "  status                TEXT    NOT NULL DEFAULT 'Active'," +
                "  helpful_count         INTEGER NOT NULL DEFAULT 0," +
                "  unhelpful_count       INTEGER NOT NULL DEFAULT 0," +
                "  edit_duration_seconds INTEGER NOT NULL DEFAULT 86400," +
                "  UNIQUE(product_id, customer_id)" +                 // no duplicate reviews
                ");");

            st.execute(
                "CREATE TABLE IF NOT EXISTS reviews_helpful_votes (" +
                "  id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  review_id   INTEGER NOT NULL REFERENCES reviews_reviews(id)," +
                "  customer_id INTEGER NOT NULL REFERENCES reviews_users(id)," +
                "  vote_type   TEXT    NOT NULL CHECK(vote_type IN ('helpful','unhelpful'))," +
                "  created_at  INTEGER NOT NULL," +
                "  UNIQUE(review_id, customer_id)" +                  // one vote per customer per review
                ");");

            st.execute(
                "CREATE TABLE IF NOT EXISTS reviews_review_flags (" +
                "  id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  review_id   INTEGER NOT NULL REFERENCES reviews_reviews(id)," +
                "  customer_id INTEGER NOT NULL REFERENCES reviews_users(id)," +
                "  reason      TEXT    NOT NULL," +
                "  created_at  INTEGER NOT NULL," +
                "  resolved_at INTEGER," +
                "  resolved_by INTEGER REFERENCES reviews_users(id)," +
                "  UNIQUE(review_id, customer_id)" +
                ");");
            ensureColumnExists("reviews_review_flags", "resolved_at", "INTEGER");
            ensureColumnExists(
                    "reviews_review_flags",
                    "resolved_by",
                    "INTEGER REFERENCES reviews_users(id)");

            st.execute(
                "CREATE TABLE IF NOT EXISTS reviews_admin_moderation_log (" +
                "  id         INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  admin_id   INTEGER NOT NULL REFERENCES reviews_users(id)," +
                "  review_id  INTEGER NOT NULL REFERENCES reviews_reviews(id)," +
                "  action     TEXT    NOT NULL," +
                "  created_at INTEGER NOT NULL," +
                "  notes      TEXT" +
                ");");

            // Recreate the view so older databases stop exposing disabled
            // Reviews-only sample products beside the shared Product catalogue.
            st.execute("DROP VIEW IF EXISTS reviews_products;");
            st.execute(
                "CREATE VIEW reviews_products AS " +
                "SELECT product.productID AS id, product.productName AS name, product.price, " +
                "product.stockQuantity AS stock, COALESCE(category.category, 'Meals') AS category, " +
                "COALESCE(rating.average_rating, 0) AS average_rating " +
                "FROM product_Products product " +
                "LEFT JOIN (SELECT productID, MAX(chosenCuisines) AS category " +
                "           FROM product_Category GROUP BY productID) category " +
                "  ON category.productID = product.productID " +
                "LEFT JOIN (SELECT productID, CASE WHEN SUM(noPeople)=0 THEN 0 " +
                "                  ELSE SUM(rating*noPeople)/SUM(noPeople) END AS average_rating " +
                "           FROM product_Ratings GROUP BY productID) rating " +
                "  ON rating.productID = product.productID " +
                "WHERE product.status=1;");
        }
    }

    /** Insert sample rows only if the users table is empty. */
    private void seedIfEmpty() throws SQLException {
        boolean empty;
        try (Statement st = connection.createStatement();
             java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM reviews_users")) {
            rs.next();
            empty = rs.getInt(1) == 0;
        }
        if (!empty) {
            return;
        }
        System.out.println("[Database] Seeding sample data...");

        long now = Instant.now().toEpochMilli();

        try (Statement st = connection.createStatement()) {
            // Users (1 admin, 3 customers). Passwords are min 8 chars (FR1).
            st.executeUpdate("INSERT INTO reviews_users(name,email,password,role,address) VALUES " +
                "('Site Admin','admin@loop.com','admin123','ADMIN',NULL)," +
                "('Tasmia Biswas','tasmia@loop.com','password1','CUSTOMER','12 Baker St, London')," +
                "('Daniel Okafor','daniel@loop.com','password2','CUSTOMER','8 Oak Rd, Leeds')," +
                "('Priya Sharma','priya@loop.com','password3','CUSTOMER','5 Elm Ave, Bristol');");

            int[] products = ensureSampleProducts();

            // Orders establish purchase eligibility (verifyPurchase / FR2).
            // Tasmia(2) bought products 1,2,3 ; Daniel(3) bought 1,4 ; Priya(4) bought 2,5.
            String d = "2026-07-10";
            st.executeUpdate("INSERT INTO reviews_orders(customer_id,product_id,order_date) VALUES " +
                "(2," + products[0] + ",'" + d + "'),(2," + products[1] + ",'" + d + "')," +
                "(2," + products[2] + ",'" + d + "'),(3," + products[0] + ",'" + d + "')," +
                "(3," + products[3] + ",'" + d + "'),(4," + products[1] + ",'" + d + "')," +
                "(4," + products[4] + ",'" + d + "');");

            // A few seeded reviews. Older created_at so most are already locked,
            // demonstrating the edit window; one is fresh so editing can be tried.
            long tenMinAgo = now - (10L * 60 * 1000);
            long dayAgo = now - (24L * 60 * 60 * 1000);
            st.executeUpdate("INSERT INTO reviews_reviews(product_id,customer_id,rating,comment_text,image_url,created_at,status,helpful_count,unhelpful_count,edit_duration_seconds) VALUES " +
                "(" + products[0] + ",3,5,'Absolutely delicious, cooked perfectly and arrived hot.',NULL," + dayAgo + ",'Active',4,0,300)," +
                "(" + products[0] + ",2,4,'Really tasty salmon, portion could be a little bigger.',NULL," + tenMinAgo + ",'Active',2,1,300)," +
                "(" + products[1] + ",4,5,'Best vegan bowl I have had, so fresh and filling.',NULL," + dayAgo + ",'Active',6,0,300)," +
                "(" + products[2] + ",2,3,'Broth was good but noodles were slightly overcooked.',NULL," + dayAgo + ",'Active',1,0,300)," +
                "(" + products[4] + ",4,4,'Lovely dessert, sweet and creamy.',NULL," + dayAgo + ",'Active',3,0,300);");
        }

        System.out.println("[Database] Sample data seeded.");
    }

    /** Adds a stable pool of named demo reviewers without duplicating accounts. */
    private void seedReviewUsers() throws SQLException {
        String sql = "INSERT OR IGNORE INTO reviews_users " +
                "(name,email,password,role,address) VALUES(?,?,'review123','CUSTOMER',?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < TARGET_REVIEWS_PER_PRODUCT; index++) {
                statement.setString(1, reviewerName(index));
                statement.setString(2, String.format("reviewer-%03d@loop.demo", index + 1));
                statement.setString(3, (10 + (index % 80)) + " Sakura Avenue, London");
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void ensureColumnExists(String table, String column, String definition)
            throws SQLException {
        boolean exists = false;
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (result.next()) {
                if (column.equalsIgnoreCase(result.getString("name"))) {
                    exists = true;
                    break;
                }
            }
        }
        if (!exists) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE " + table + " ADD COLUMN "
                        + column + " " + definition);
            }
        }
    }

    private String reviewerName(int index) {
        if (index < FAN_FAVOURITE_REVIEWERS.length) {
            return FAN_FAVOURITE_REVIEWERS[index];
        }
        int generatedIndex = index - FAN_FAVOURITE_REVIEWERS.length;
        return REVIEWER_FIRST_NAMES[generatedIndex % REVIEWER_FIRST_NAMES.length] + " " +
                REVIEWER_LAST_NAMES[(generatedIndex / REVIEWER_FIRST_NAMES.length)
                        % REVIEWER_LAST_NAMES.length];
    }

    /**
     * Tops up each active Product item to 100 real, visible review rows. The
     * generated ratings preserve the product's existing average while dates,
     * wording and helpful votes vary. Existing reviews are never overwritten.
     */
    private void seedCatalogueReviews() throws SQLException {
        List<Integer> customerIds = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM reviews_users WHERE role='CUSTOMER' ORDER BY id");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                customerIds.add(result.getInt("id"));
            }
        }
        if (customerIds.isEmpty()) {
            return;
        }

        String productsSql =
                "SELECT p.productID, p.productName, " +
                "COALESCE((SELECT CASE WHEN SUM(r.noPeople)=0 THEN 0 " +
                " ELSE SUM(r.rating*r.noPeople)/SUM(r.noPeople) END " +
                " FROM product_Ratings r WHERE r.productID=p.productID), 0) AS legacy_rating " +
                "FROM product_Products p " +
                "WHERE p.status=1 " +
                "ORDER BY p.productID";

        int inserted = 0;
        try (PreparedStatement products = connection.prepareStatement(productsSql);
             ResultSet productRows = products.executeQuery();
             PreparedStatement existingReviews = connection.prepareStatement(
                     "SELECT customer_id,rating FROM reviews_reviews WHERE product_id=?");
             PreparedStatement insertReview = connection.prepareStatement(
                     "INSERT OR IGNORE INTO reviews_reviews " +
                     "(product_id,customer_id,rating,comment_text,image_url,created_at,status," +
                     "helpful_count,unhelpful_count,edit_duration_seconds) " +
                     "VALUES(?,?,?,?,NULL,?,'Active',?,?,300)")) {
            while (productRows.next()) {
                int productId = productRows.getInt("productID");
                String productName = productRows.getString("productName");
                double targetAverage = productRows.getDouble("legacy_rating");
                Set<Integer> existingCustomerIds = new HashSet<>();
                int existingRatingTotal = 0;
                existingReviews.setInt(1, productId);
                try (ResultSet reviews = existingReviews.executeQuery()) {
                    while (reviews.next()) {
                        existingCustomerIds.add(reviews.getInt("customer_id"));
                        existingRatingTotal += reviews.getInt("rating");
                    }
                }

                int needed = TARGET_REVIEWS_PER_PRODUCT - existingCustomerIds.size();
                if (needed <= 0) {
                    continue;
                }
                List<Integer> availableCustomerIds = new ArrayList<>();
                for (Integer customerId : customerIds) {
                    if (!existingCustomerIds.contains(customerId)) {
                        availableCustomerIds.add(customerId);
                        if (availableCustomerIds.size() == needed) {
                            break;
                        }
                    }
                }
                int actualNeeded = availableCustomerIds.size();
                int[] ratings = representativeRatings(
                        targetAverage, existingRatingTotal, actualNeeded, productId);
                Random detailRandom = new Random(20260805L + productId * 101L);

                for (int index = 0; index < actualNeeded; index++) {
                    insertReview.setInt(1, productId);
                    insertReview.setInt(2, availableCustomerIds.get(index));
                    insertReview.setInt(3, ratings[index]);
                    insertReview.setString(4, sampleComment(ratings[index], index, productName));
                    insertReview.setLong(5, Instant.now().minusSeconds(
                            (long) (3 + detailRandom.nextInt(360)) * 86_400L
                                    + detailRandom.nextInt(86_400)).toEpochMilli());
                    insertReview.setInt(6, detailRandom.nextInt(18) + Math.max(0, ratings[index] - 3));
                    insertReview.setInt(7, ratings[index] <= 2 ? detailRandom.nextInt(5) : detailRandom.nextInt(2));
                    inserted += insertReview.executeUpdate();
                }
            }
        }

        if (inserted > 0) {
            System.out.println("[Database] Added " + inserted
                    + " reviews to build the shared Product review history.");
        }
    }

    private int[] representativeRatings(
            double average,
            int existingRatingTotal,
            int count,
            int productId) {
        double safeAverage = average <= 0 ? 4.2 : Math.max(1.0, Math.min(5.0, average));
        int targetGrandTotal = (int) Math.round(safeAverage * TARGET_REVIEWS_PER_PRODUCT);
        int targetTotal = Math.max(count,
                Math.min(count * 5, targetGrandTotal - existingRatingTotal));
        int[] ratings = new int[count];
        Random random = new Random(20260805L + productId * 997L);
        int currentTotal = 0;
        for (int index = 0; index < count; index++) {
            ratings[index] = Math.max(1, Math.min(5,
                    (int) Math.round(safeAverage + random.nextGaussian() * 0.9)));
            currentTotal += ratings[index];
        }
        while (currentTotal != targetTotal && count > 0) {
            int index = random.nextInt(count);
            if (currentTotal < targetTotal && ratings[index] < 5) {
                ratings[index]++;
                currentTotal++;
            } else if (currentTotal > targetTotal && ratings[index] > 1) {
                ratings[index]--;
                currentTotal--;
            }
        }
        return ratings;
    }

    private String sampleComment(int rating, int index, String productName) {
        String[][] openings = {
            {"The flavours did not come together for me", "I was disappointed by this order", "This needs more work"},
            {"The dish was acceptable but uneven", "There were a few good ideas here", "It was fine, though not memorable"},
            {"A solid meal overall", "I enjoyed most of this dish", "Good value for a weekday order"},
            {"Really enjoyable from the first bite", "Fresh and very well balanced", "A strong choice that I would order again"},
            {"Excellent food and careful preparation", "One of my favourite recent orders", "Absolutely delicious from start to finish"}
        };
        String[] details = {
            "The portion size felt right and the packaging kept everything tidy.",
            "Seasoning was balanced, with a pleasant texture and a fresh finish.",
            "Delivery temperature was good and the ingredients tasted fresh.",
            "The presentation was neat and the description matched what arrived.",
            "I especially liked the texture, although the seasoning could be a little bolder.",
            "It worked well as a complete meal and did not feel too heavy.",
            "The ingredients were easy to identify and tasted thoughtfully prepared.",
            "Portion and price felt fair compared with similar meals I have tried.",
            "Everything travelled well and still looked appealing when opened.",
            "I would appreciate a little more sauce, but the main flavours were satisfying."
        };
        String opening = openings[Math.max(1, Math.min(5, rating)) - 1][index % 3];
        return opening + " for " + productName + ". " + details[(index * 7 + rating) % details.length];
    }

    /** Moves automatically-detected content out of every customer-facing query. */
    private void flagDetectedReviews() throws SQLException {
        List<Integer> reviewIds = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT id,comment_text FROM reviews_reviews WHERE status='Active'")) {
            while (result.next()) {
                if (ContentModeration.shouldHide(result.getString("comment_text"))) {
                    reviewIds.add(result.getInt("id"));
                }
            }
        }
        if (reviewIds.isEmpty()) {
            return;
        }
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE reviews_reviews SET status='Flagged' WHERE id=? AND status='Active'")) {
            for (Integer reviewId : reviewIds) {
                update.setInt(1, reviewId);
                update.addBatch();
            }
            update.executeBatch();
        }
        System.out.println("[Database] Automatically hidden " + reviewIds.size()
                + " review(s) pending administrator moderation.");
    }

    /**
     * Gives a Customer-session identity a small, stable purchase and review
     * history so My Reviews belongs to the customer who entered from the hub.
     */
    public void ensureCustomerReviewActivity(int customerId) {
        String productsSql =
                "SELECT productID,productName FROM product_Products " +
                "WHERE status=1 ORDER BY productID LIMIT 6";
        String orderSql =
                "INSERT INTO reviews_orders(customer_id,product_id,order_date) " +
                "SELECT ?,?,'2026-07-18' WHERE NOT EXISTS " +
                "(SELECT 1 FROM reviews_orders WHERE customer_id=? AND product_id=?)";
        String reviewSql =
                "INSERT OR IGNORE INTO reviews_reviews " +
                "(product_id,customer_id,rating,comment_text,image_url,created_at,status," +
                "helpful_count,unhelpful_count,edit_duration_seconds) " +
                "VALUES(?,?,?,?,NULL,?,'Active',0,0,300)";

        List<Integer> touchedProducts = new ArrayList<>();
        try (PreparedStatement products = connection.prepareStatement(productsSql);
             ResultSet productRows = products.executeQuery();
             PreparedStatement order = connection.prepareStatement(orderSql);
             PreparedStatement review = connection.prepareStatement(reviewSql)) {
            int index = 0;
            while (productRows.next()) {
                int productId = productRows.getInt("productID");
                String productName = productRows.getString("productName");

                order.setInt(1, customerId);
                order.setInt(2, productId);
                order.setInt(3, customerId);
                order.setInt(4, productId);
                order.executeUpdate();

                int rating = index % 3 == 0 ? 5 : 4;
                review.setInt(1, productId);
                review.setInt(2, customerId);
                review.setInt(3, rating);
                review.setString(4, "I enjoyed " + productName
                        + "; it arrived fresh, well packed and matched the menu description.");
                review.setLong(5, Instant.parse("2026-07-20T12:00:00Z")
                        .plusSeconds(index * 93_600L).toEpochMilli());
                if (review.executeUpdate() > 0) {
                    touchedProducts.add(productId);
                }
                index++;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Could not prepare Customer review activity", exception);
        }

        ProductDao products = new ProductDao();
        for (Integer productId : touchedProducts) {
            products.recalculateAverage(productId);
        }
    }

    /**
     * Keeps several purchased-but-unreviewed foods available for the review
     * validation video. Existing reviews are deliberately left untouched, so
     * each successful practice run can move to the next eligible food.
     */
    public void ensureReviewSubmissionDemoPurchases(int customerId) {
        String productsSql =
                "SELECT productID FROM product_Products " +
                "WHERE status=1 AND productName IN " +
                "('Vegetable Gyoza','Beef Bulgogi','Thai Green Curry'," +
                "'Falafel Mezze Bowl','Mediterranean Salmon') " +
                "ORDER BY productID";
        String orderSql =
                "INSERT INTO reviews_orders(customer_id,product_id,order_date) " +
                "SELECT ?,?,'2026-08-01' WHERE NOT EXISTS " +
                "(SELECT 1 FROM reviews_orders WHERE customer_id=? AND product_id=?)";

        try (PreparedStatement products = connection.prepareStatement(productsSql);
             ResultSet productRows = products.executeQuery();
             PreparedStatement order = connection.prepareStatement(orderSql)) {
            while (productRows.next()) {
                int productId = productRows.getInt("productID");
                order.setInt(1, customerId);
                order.setInt(2, productId);
                order.setInt(3, customerId);
                order.setInt(4, productId);
                order.addBatch();
            }
            order.executeBatch();
        } catch (SQLException exception) {
            throw new RuntimeException("Could not prepare review demonstration purchases", exception);
        }
    }

    private int[] ensureSampleProducts() throws SQLException {
        int chefId = 0;
        try (Statement statement = connection.createStatement();
             java.sql.ResultSet result = statement.executeQuery("SELECT MIN(chefID) FROM product_Chef")) {
            if (result.next()) {
                chefId = result.getInt(1);
            }
        }
        if (chefId == 0) {
            try (java.sql.PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO product_Chef " +
                    "(chefName,\"chefRating&ReviewID\",chefDescription,chefTag1,chefTag2,chefTag3," +
                    "chefImage,chefEmail,chefTel) VALUES " +
                    "('LOOP Reviews',0,'Reviews catalogue chef','','','','','reviews@loop.com','')",
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.executeUpdate();
                try (java.sql.ResultSet keys = statement.getGeneratedKeys()) {
                    keys.next();
                    chefId = keys.getInt(1);
                }
            }
        }

        String[] names = {
            "Grilled Salmon Bowl", "Vegan Buddha Bowl", "Spicy Chicken Ramen",
            "Classic Beef Burger", "Mango Sticky Rice"
        };
        double[] prices = {12.99, 9.50, 11.25, 10.00, 6.75};
        int[] stocks = {40, 60, 25, 0, 50};
        String[] categories = {"Mains", "Vegan", "Mains", "Mains", "Desserts"};
        int[] ids = new int[names.length];

        for (int index = 0; index < names.length; index++) {
            try (java.sql.PreparedStatement find = connection.prepareStatement(
                    "SELECT productID FROM product_Products WHERE productName=? LIMIT 1")) {
                find.setString(1, names[index]);
                try (java.sql.ResultSet result = find.executeQuery()) {
                    if (result.next()) {
                        ids[index] = result.getInt(1);
                    }
                }
            }

            if (ids[index] == 0) {
                try (java.sql.PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO product_Products " +
                        "(productName,shortDescription,extendedDescription,cost,price,status,spiceLevel," +
                        "country,chefID,sourceModule,stockQuantity) VALUES " +
                        "(?,'Reviews catalogue item','Reviews demonstration product',?,?,0,0," +
                        "'United Kingdom',?,'reviews',?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    insert.setString(1, names[index]);
                    insert.setDouble(2, Math.round(prices[index] * 50.0) / 100.0);
                    insert.setDouble(3, prices[index]);
                    insert.setInt(4, chefId);
                    insert.setInt(5, stocks[index]);
                    insert.executeUpdate();
                    try (java.sql.ResultSet keys = insert.getGeneratedKeys()) {
                        keys.next();
                        ids[index] = keys.getInt(1);
                    }
                }

                try (java.sql.PreparedStatement category = connection.prepareStatement(
                        "INSERT INTO product_Category " +
                        "(chosenSortBy,chosenDietary,chosenHealthGoal,chosenCuisines,productID) " +
                        "VALUES('','Not specified','Balanced Meals',?,?)")) {
                    category.setString(1, categories[index]);
                    category.setInt(2, ids[index]);
                    category.executeUpdate();
                }
                try (java.sql.PreparedStatement rating = connection.prepareStatement(
                        "INSERT INTO product_Ratings(rating,noPeople,productID) VALUES(0,0,?)")) {
                    rating.setInt(1, ids[index]);
                    rating.executeUpdate();
                }
            }
        }
        return ids;
    }
}
