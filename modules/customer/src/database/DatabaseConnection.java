package database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import utils.PasswordUtil;

public class DatabaseConnection {

    private static final String MAIN_DB_URL = "jdbc:sqlite:" +
            System.getProperty("loop.db.path", "database/loop.db");
    private static String dbUrl = MAIN_DB_URL;
    private static String initializedUrl;
    private static final int TARGET_CHEF_REVIEWS = 40;
    private static final String[] REVIEWER_NAMES = {
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
    private static final DemoCustomer[] DEMO_CUSTOMERS = {
        new DemoCustomer("001", "Demo Customer", "customer@loop.com", "customer123",
                "07123456789", "1 Loop Street, London"),
        new DemoCustomer("002", "Maya Patel", "maya@loop.demo", "demo123",
                "07123456790", "12 Maple Road, London"),
        new DemoCustomer("003", "Noah Williams", "noah@loop.demo", "demo123",
                "07123456791", "24 River Lane, Kingston"),
        new DemoCustomer("004", "Zara Khan", "zara@loop.demo", "demo123",
                "07123456792", "8 Market Street, Richmond"),
        new DemoCustomer("005", "Liam Chen", "liam@loop.demo", "demo123",
                "07123456793", "41 Garden Avenue, Hounslow"),
        new DemoCustomer("006", "Aisha Rahman", "aisha@loop.demo", "demo123",
                "07123456794", "17 Park View, Croydon"),
        new DemoCustomer("007", "Oliver Green", "oliver@loop.demo", "demo123",
                "07123456795", "6 Station Road, Wimbledon")
    };
    private static final DemoMenuItem[] DEMO_MENU_ITEMS = {
        new DemoMenuItem(1, "Kimchi Fried Rice", 9.99),
        new DemoMenuItem(2, "Salmon & Quinoa Bowl", 11.99),
        new DemoMenuItem(7, "Miso Tofu Ramen", 9.49),
        new DemoMenuItem(11, "Thai Green Curry", 10.49),
        new DemoMenuItem(17, "Chicken Shawarma Bowl", 10.99),
        new DemoMenuItem(22, "Margherita Flatbread", 7.99)
    };

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    /** Used by JUnit tests so the main database is not modified. */
    public static void useTestDatabase() {
        dbUrl = "jdbc:sqlite:loop_customers_test.db";
    }

    public static void useMainDatabase() {
        dbUrl = MAIN_DB_URL;
    }

    public static synchronized void initializeDatabase() {
        if (dbUrl.equals(initializedUrl)) {
            return;
        }

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customer_People (
                    personID TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    mobile TEXT NOT NULL,
                    passwordHash TEXT NOT NULL
                );""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customer_Customers (
                    customerID TEXT PRIMARY KEY,
                    personID TEXT NOT NULL,
                    deliveryAddress TEXT NOT NULL,
                    idCardNo TEXT UNIQUE NOT NULL,
                    idCardImagePath TEXT,
                    status TEXT NOT NULL DEFAULT 'Active',
                    FOREIGN KEY (personID) REFERENCES customer_People(personID)
                );""");

            ensureColumnExists(conn, "customer_Customers", "idCardImagePath", "TEXT");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customer_CustomerPreference (
                    preferenceID TEXT PRIMARY KEY,
                    customerID TEXT NOT NULL,
                    favoriteCategories TEXT,
                    notificationSettings TEXT,
                    deliveryInstructions TEXT,
                    FOREIGN KEY (customerID) REFERENCES customer_Customers(customerID)
                );""");

            stmt.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS customer_UQ_CustomerPreference_Customer
                ON customer_CustomerPreference(customerID);""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS orders_Orders (
                    orderID INTEGER PRIMARY KEY AUTOINCREMENT,
                    customerID TEXT,
                    customerName TEXT NOT NULL DEFAULT 'Guest',
                    orderDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    status TEXT NOT NULL DEFAULT 'Pending',
                    totalAmount REAL NOT NULL DEFAULT 0,
                    FOREIGN KEY (customerID) REFERENCES customer_Customers(customerID)
                );""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS orders_OrderItems (
                    orderItemID INTEGER PRIMARY KEY AUTOINCREMENT,
                    orderID INTEGER NOT NULL,
                    productID INTEGER NOT NULL,
                    itemName TEXT NOT NULL,
                    quantity INTEGER NOT NULL DEFAULT 1,
                    priceAtOrder REAL NOT NULL DEFAULT 0,
                    FOREIGN KEY (orderID) REFERENCES orders_Orders(orderID),
                    FOREIGN KEY (productID) REFERENCES product_Products(productID)
                );""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customer_Chefs (
                    chefID TEXT PRIMARY KEY,
                    chefName TEXT NOT NULL,
                    speciality TEXT,
                    averageRating REAL DEFAULT 0
                );""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customer_ChefReviews (
                    reviewID TEXT PRIMARY KEY,
                    customerID TEXT NOT NULL,
                    chefID TEXT NOT NULL,
                    rating INTEGER NOT NULL CHECK(rating >= 1 AND rating <= 5),
                    reviewText TEXT NOT NULL,
                    createdAt TEXT NOT NULL,
                    FOREIGN KEY (customerID) REFERENCES customer_Customers(customerID),
                    FOREIGN KEY (chefID) REFERENCES customer_Chefs(chefID),
                    UNIQUE(customerID, chefID)
                );""");

                seedDummyCustomers(conn);
                seedReviewerCustomers(conn);
                seedChefs(conn);
                seedDummyActivity(conn);
                conn.commit();
                initializedUrl = dbUrl;
                System.out.println("Database initialized successfully.");
            } catch (SQLException exception) {
                conn.rollback();
                throw exception;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void ensureColumnExists(Connection conn, String table, String column, String type)
            throws SQLException {
        boolean found = false;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            try (Statement st = conn.createStatement()) {
                st.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
            }
        }
    }

    private static void seedChefs(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM customer_Chefs")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }

        String[][] chefs = {
            {"Gordon Ramsay",     "Fine Dining & British Cuisine",  "4.8"},
            {"Jamie Oliver",      "Italian & Mediterranean",         "4.6"},
            {"Yotam Ottolenghi", "Middle Eastern & Vegetarian",    "4.7"},
            {"Nigella Lawson",   "Comfort Food & Baking",          "4.5"},
            {"Heston Blumenthal","Molecular Gastronomy",            "4.9"},
            {"Rick Stein",       "Seafood & Fish Dishes",          "4.4"},
            {"Ainsley Harriott", "Caribbean & Fusion",              "4.3"},
            {"Nadiya Hussain",   "South Asian & Baking",           "4.6"},
        };

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO customer_Chefs (chefID, chefName, speciality, averageRating) VALUES (?, ?, ?, ?)")) {
            for (String[] c : chefs) {
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, c[0]);
                ps.setString(3, c[1]);
                ps.setDouble(4, Double.parseDouble(c[2]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Adds reusable demo logins without replacing preferences edited by a user. */
    private static void seedDummyCustomers(Connection conn) throws SQLException {
        String insertPerson = """
            INSERT OR IGNORE INTO customer_People
                (personID, name, email, mobile, passwordHash)
            VALUES (?, ?, ?, ?, ?)
        """;
        String insertCustomer = """
            INSERT OR IGNORE INTO customer_Customers
                (customerID, personID, deliveryAddress, idCardNo, status)
            VALUES (?, ?, ?, ?, 'Active')
        """;
        String insertPreference = """
            INSERT OR IGNORE INTO customer_CustomerPreference
                (preferenceID, customerID, favoriteCategories,
                 notificationSettings, deliveryInstructions)
            VALUES (?, ?, ?, 'Enabled', '')
        """;

        Random preferenceRandom = new Random(20260805L);
        Set<String> usedPreferenceProfiles = new HashSet<>();
        try (PreparedStatement personStatement = conn.prepareStatement(insertPerson);
             PreparedStatement customerStatement = conn.prepareStatement(insertCustomer);
             PreparedStatement preferenceStatement = conn.prepareStatement(insertPreference)) {
            for (int index = 0; index < DEMO_CUSTOMERS.length; index++) {
                DemoCustomer customer = DEMO_CUSTOMERS[index];
                String personID = "demo-person-" + customer.id();
                String customerID = "demo-customer-" + customer.id();

                personStatement.setString(1, personID);
                personStatement.setString(2, customer.name());
                personStatement.setString(3, customer.email());
                personStatement.setString(4, customer.mobile());
                personStatement.setString(5, PasswordUtil.hash(customer.password()));
                personStatement.addBatch();

                customerStatement.setString(1, customerID);
                customerStatement.setString(2, personID);
                customerStatement.setString(3, customer.address());
                customerStatement.setString(4, "LOOP-DEMO-" + customer.id());
                customerStatement.addBatch();

                preferenceStatement.setString(1, "demo-preference-" + customer.id());
                preferenceStatement.setString(2, customerID);
                preferenceStatement.setString(3, index == 0
                        ? "General"
                        : nextUniqueRandomPreferences(
                                preferenceRandom,
                                usedPreferenceProfiles));
                preferenceStatement.addBatch();
            }

            personStatement.executeBatch();
            customerStatement.executeBatch();
            preferenceStatement.executeBatch();
        }
    }

    /** Review-focused demo profiles shared by the chef review history. */
    private static void seedReviewerCustomers(Connection conn) throws SQLException {
        String insertPerson = """
            INSERT OR IGNORE INTO customer_People
                (personID, name, email, mobile, passwordHash)
            VALUES (?, ?, ?, ?, ?)
        """;
        String insertCustomer = """
            INSERT OR IGNORE INTO customer_Customers
                (customerID, personID, deliveryAddress, idCardNo, status)
            VALUES (?, ?, ?, ?, 'Active')
        """;
        String insertPreference = """
            INSERT OR IGNORE INTO customer_CustomerPreference
                (preferenceID, customerID, favoriteCategories,
                 notificationSettings, deliveryInstructions)
            VALUES (?, ?, ?, 'Enabled', ?)
        """;

        String passwordHash = PasswordUtil.hash("review123");
        Random preferenceRandom = new Random(20260806L);
        try (PreparedStatement person = conn.prepareStatement(insertPerson);
             PreparedStatement customer = conn.prepareStatement(insertCustomer);
             PreparedStatement preference = conn.prepareStatement(insertPreference)) {
            for (int index = 0; index < REVIEWER_NAMES.length; index++) {
                int number = index + 1;
                String suffix = String.format("%03d", number);
                String customerID = "reviewer-customer-" + suffix;

                person.setString(1, "reviewer-person-" + suffix);
                person.setString(2, REVIEWER_NAMES[index]);
                person.setString(3, "reviewer-" + suffix + "@loop.demo");
                person.setString(4, String.format("07200%06d", number));
                person.setString(5, passwordHash);
                person.addBatch();

                customer.setString(1, customerID);
                customer.setString(2, "reviewer-person-" + suffix);
                customer.setString(3, (20 + number) + " Sakura Avenue, London");
                customer.setString(4, "LOOP-REVIEWER-" + suffix);
                customer.addBatch();

                preference.setString(1, "reviewer-preference-" + suffix);
                preference.setString(2, customerID);
                preference.setString(3, randomPreferences(preferenceRandom));
                preference.setString(4, number % 3 == 0
                        ? "Please leave the order by the door."
                        : "Ring the bell on arrival.");
                preference.addBatch();
            }
            person.executeBatch();
            customer.executeBatch();
            preference.executeBatch();
        }
    }

    private static void seedDummyActivity(Connection conn) throws SQLException {
        seedDummyOrders(conn);
        seedDummyReviews(conn);
    }

    private static void seedDummyOrders(Connection conn) throws SQLException {
        String[] activeStatuses = {
            "Confirmed", "Preparing", "Out For Delivery", "Pending",
            "Confirmed", "Out For Delivery", "Preparing"
        };

        for (int index = 0; index < DEMO_CUSTOMERS.length; index++) {
            DemoCustomer customer = DEMO_CUSTOMERS[index];
            DemoMenuItem first = DEMO_MENU_ITEMS[index % DEMO_MENU_ITEMS.length];
            DemoMenuItem second = DEMO_MENU_ITEMS[(index + 2) % DEMO_MENU_ITEMS.length];
            String deliveredDate = String.format(
                    "2026-07-%02d 18:%02d:00", 20 + index, 10 + index);
            String activeDate = String.format(
                    "2026-08-%02d 12:%02d:00", 1 + (index % 4), 20 + index);

            seedOrder(conn, customer, deliveredDate, "Delivered",
                    first, 1, second, 1);
            seedOrder(conn, customer, activeDate, activeStatuses[index],
                    second, 2, first, 1);
        }
    }

    private static void seedOrder(
            Connection conn,
            DemoCustomer customer,
            String orderDate,
            String status,
            DemoMenuItem first,
            int firstQuantity,
            DemoMenuItem second,
            int secondQuantity) throws SQLException {
        String customerID = "demo-customer-" + customer.id();
        double total = first.price() * firstQuantity + second.price() * secondQuantity;
        String insertOrder = """
            INSERT INTO orders_Orders
                (customerID, customerName, orderDate, status, totalAmount)
            SELECT ?, ?, ?, ?, ?
            WHERE NOT EXISTS (
                SELECT 1 FROM orders_Orders
                WHERE customerID = ? AND orderDate = ?
            )
        """;

        try (PreparedStatement statement = conn.prepareStatement(insertOrder)) {
            statement.setString(1, customerID);
            statement.setString(2, customer.name());
            statement.setString(3, orderDate);
            statement.setString(4, status);
            statement.setDouble(5, Math.round(total * 100.0) / 100.0);
            statement.setString(6, customerID);
            statement.setString(7, orderDate);
            statement.executeUpdate();
        }

        int orderID = findSeededOrderID(conn, customerID, orderDate);
        seedOrderItem(conn, orderID, first, firstQuantity);
        seedOrderItem(conn, orderID, second, secondQuantity);
    }

    private static int findSeededOrderID(
            Connection conn,
            String customerID,
            String orderDate) throws SQLException {
        String sql = "SELECT orderID FROM orders_Orders WHERE customerID = ? AND orderDate = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, customerID);
            statement.setString(2, orderDate);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt(1);
                }
            }
        }
        throw new SQLException("Could not find seeded order for " + customerID);
    }

    private static void seedOrderItem(
            Connection conn,
            int orderID,
            DemoMenuItem item,
            int quantity) throws SQLException {
        String sql = """
            INSERT INTO orders_OrderItems
                (orderID, productID, itemName, quantity, priceAtOrder)
            SELECT ?, ?, ?, ?, ?
            WHERE NOT EXISTS (
                SELECT 1 FROM orders_OrderItems
                WHERE orderID = ? AND productID = ?
            )
        """;
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, orderID);
            statement.setInt(2, item.productID());
            statement.setString(3, item.name());
            statement.setInt(4, quantity);
            statement.setDouble(5, item.price());
            statement.setInt(6, orderID);
            statement.setInt(7, item.productID());
            statement.executeUpdate();
        }
    }

    private static void seedDummyReviews(Connection conn) throws SQLException {
        List<String> chefIDs = new ArrayList<>();
        try (Statement statement = conn.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT chefID FROM customer_Chefs ORDER BY chefName")) {
            while (result.next()) {
                chefIDs.add(result.getString(1));
            }
        }
        if (chefIDs.size() < 2) {
            return;
        }

        String[] reviewTexts = {
            "The meal was colourful, fresh and arrived at the perfect temperature.",
            "Really enjoyable flavours and generous portions; I would order this again.",
            "The presentation was excellent and the dietary options were clearly explained.",
            "Quick service, balanced seasoning and a very satisfying meal overall.",
            "A creative menu with good texture and plenty of flavour in every bite.",
            "Fresh ingredients and thoughtful preparation made this a memorable order.",
            "The dish matched my preferences well and the portion size felt just right."
        };
        String sql = """
            INSERT OR IGNORE INTO customer_ChefReviews
                (reviewID, customerID, chefID, rating, reviewText, createdAt)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            for (int index = 0; index < DEMO_CUSTOMERS.length; index++) {
                DemoCustomer customer = DEMO_CUSTOMERS[index];
                for (int reviewIndex = 0; reviewIndex < 2; reviewIndex++) {
                    int chefIndex = (index + reviewIndex * 3) % chefIDs.size();
                    statement.setString(1, "demo-review-" + customer.id() + "-" + reviewIndex);
                    statement.setString(2, "demo-customer-" + customer.id());
                    statement.setString(3, chefIDs.get(chefIndex));
                    statement.setInt(4, 3 + ((index + reviewIndex) % 3));
                    statement.setString(5,
                            reviewTexts[(index + reviewIndex) % reviewTexts.length]);
                    statement.setString(6, String.format(
                            "2026-07-%02d", 21 + ((index + reviewIndex) % 8)));
                    statement.addBatch();
                }
            }
            statement.executeBatch();
        }

        seedExtendedChefReviews(conn, chefIDs);

        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate("""
                UPDATE customer_Chefs
                SET averageRating = COALESCE((
                    SELECT ROUND(AVG(review.rating), 1)
                    FROM customer_ChefReviews review
                    WHERE review.chefID = customer_Chefs.chefID
                ), averageRating)
            """);
        }
    }

    private static void seedExtendedChefReviews(Connection conn, List<String> chefIDs)
            throws SQLException {
        List<String> reviewerCustomerIDs = new ArrayList<>();
        try (Statement statement = conn.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT customerID FROM customer_Customers " +
                     "WHERE customerID LIKE 'reviewer-customer-%' ORDER BY customerID")) {
            while (result.next()) {
                reviewerCustomerIDs.add(result.getString(1));
            }
        }

        String[] comments = {
            "Careful preparation and confident seasoning made the whole meal feel polished.",
            "The menu was creative without being fussy, and every element worked together.",
            "Fresh ingredients, a generous portion and excellent attention to detail.",
            "The flavours were balanced and the dietary information was genuinely useful.",
            "A comforting meal with thoughtful presentation and consistently good texture.",
            "The main dish was memorable and the side choices complemented it very well.",
            "Reliable quality and a clear sense of style; I would order from this chef again.",
            "Good value overall, with bright flavours and packaging that travelled well.",
            "The seasoning was subtle but effective and the meal still tasted fresh on arrival.",
            "An enjoyable experience from ordering through to the final bite.",
            "The portion could be slightly larger, but the cooking and flavour were excellent.",
            "A well-designed dish that matched its description and felt freshly made."
        };
        String insertSql = """
            INSERT OR IGNORE INTO customer_ChefReviews
                (reviewID, customerID, chefID, rating, reviewText, createdAt)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        String existingSql = "SELECT customerID FROM customer_ChefReviews WHERE chefID=?";

        try (PreparedStatement existing = conn.prepareStatement(existingSql);
             PreparedStatement insert = conn.prepareStatement(insertSql)) {
            for (int chefIndex = 0; chefIndex < chefIDs.size(); chefIndex++) {
                String chefID = chefIDs.get(chefIndex);
                Set<String> usedCustomers = new HashSet<>();
                existing.setString(1, chefID);
                try (ResultSet reviews = existing.executeQuery()) {
                    while (reviews.next()) {
                        usedCustomers.add(reviews.getString(1));
                    }
                }

                Random random = new Random(20260805L + chefIndex * 313L);
                for (int reviewerIndex = 0;
                     reviewerIndex < reviewerCustomerIDs.size()
                             && usedCustomers.size() < TARGET_CHEF_REVIEWS;
                     reviewerIndex++) {
                    String customerID = reviewerCustomerIDs.get(reviewerIndex);
                    if (usedCustomers.contains(customerID)) {
                        continue;
                    }
                    int roll = random.nextInt(100);
                    int rating = roll < 4 ? 2 : roll < 14 ? 3 : roll < 48 ? 4 : 5;
                    String reviewID = "reviewer-chef-" + chefIndex + "-"
                            + String.format("%03d", reviewerIndex + 1);

                    insert.setString(1, reviewID);
                    insert.setString(2, customerID);
                    insert.setString(3, chefID);
                    insert.setInt(4, rating);
                    insert.setString(5, comments[(reviewerIndex * 5 + chefIndex) % comments.length]);
                    insert.setString(6, LocalDate.of(2025, 9, 1)
                            .plusDays((reviewerIndex * 11L + chefIndex * 17L) % 335L)
                            .toString());
                    if (insert.executeUpdate() > 0) {
                        usedCustomers.add(customerID);
                    }
                }
            }
        }
    }

    private static String nextUniqueRandomPreferences(
            Random random,
            Set<String> usedPreferenceProfiles) {
        while (true) {
            String candidate = randomPreferences(random);
            List<String> canonical = new ArrayList<>(List.of(candidate.split(",")));
            Collections.sort(canonical);
            if (usedPreferenceProfiles.add(String.join(",", canonical))) {
                return candidate;
            }
        }
    }

    private static String randomPreferences(Random random) {
        List<String> choices = new ArrayList<>(List.of(
                "Vegan", "Vegetarian", "Keto", "Gluten-Free", "Halal",
                "Low-Calorie", "Pescatarian", "High-Protein", "Weight-Loss"
        ));
        Collections.shuffle(choices, random);
        int preferenceCount = 2 + random.nextInt(2);
        return String.join(",", choices.subList(0, preferenceCount));
    }

    private record DemoCustomer(
            String id,
            String name,
            String email,
            String password,
            String mobile,
            String address) { }

    private record DemoMenuItem(int productID, String name, double price) { }
}
