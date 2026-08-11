package ProductPage.ProductPage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import services.InventoryDeliveryService;

public final class ProductAvailabilityService {

    private ProductAvailabilityService() {
    }

    public static Map<Integer, Availability> loadAll()
            throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        String path = System.getProperty("loop.db.path", "database/loop.db");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + path)) {
            return loadAll(connection, resolveStockYear(connection));
        }
    }

    public static Availability loadForProduct(int productId)
            throws ClassNotFoundException, SQLException {
        return loadAll().getOrDefault(productId, Availability.available());
    }

    /** Loads availability from the warehouse serving the supplied customer address. */
    public static Map<Integer, Availability> loadAllForAddress(String deliveryAddress)
            throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        String path = System.getProperty("loop.db.path", "database/loop.db");
        InventoryDeliveryService.ensureSchemaAndSeedData();
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + path)) {
            InventoryDeliveryService.WarehouseAssignment warehouse =
                    InventoryDeliveryService.resolveWarehouse(connection, deliveryAddress);
            return loadAll(
                    connection,
                    resolveStockYear(connection),
                    warehouse.getWarehouseID(),
                    warehouse.getDisplayName());
        }
    }

    static Map<Integer, Availability> loadAll(Connection connection, int stockYear)
            throws SQLException {
        return loadAll(connection, stockYear, null, null);
    }

    static Map<Integer, Availability> loadAll(
            Connection connection,
            int stockYear,
            String warehouseId,
            String locationLabel) throws SQLException {
        Map<Integer, IngredientStock> stockByIngredient = loadIngredientStock(
                connection, stockYear, warehouseId);
        Map<Integer, Set<Integer>> requiredByProduct = loadRequiredIngredients(connection);
        Map<Integer, Availability> result = new LinkedHashMap<>();

        for (Map.Entry<Integer, Set<Integer>> entry : requiredByProduct.entrySet()) {
            List<String> missing = new ArrayList<>();
            for (Integer ingredientId : entry.getValue()) {
                IngredientStock stock = stockByIngredient.get(ingredientId);
                if (stock == null) {
                    missing.add("Ingredient #" + ingredientId);
                } else if (stock.quantity <= 0) {
                    missing.add(stock.name);
                }
            }
            result.put(entry.getKey(), new Availability(missing, locationLabel));
        }

        return result;
    }

    private static int resolveStockYear(Connection connection) throws SQLException {
        int currentYear = Year.now().getValue();
        String sql = "SELECT COALESCE(MAX(CASE WHEN stockYear = " + currentYear
                + " THEN stockYear END), MAX(stockYear), " + currentYear
                + ") FROM inventory_Stock";
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            return result.next() ? result.getInt(1) : currentYear;
        }
    }

    private static Map<Integer, IngredientStock> loadIngredientStock(
            Connection connection, int stockYear, String warehouseId) throws SQLException {
        boolean locationSpecific = warehouseId != null && !warehouseId.isBlank();
        String sql = "SELECT ingredient.ingredientID, ingredient.ingredientName, "
                + "COALESCE(SUM(CASE WHEN stock.stockYear = ? "
                + (locationSpecific
                    ? "AND UPPER(TRIM(stock.warehouseID)) = ? "
                    : "")
                + "THEN stock.stockQuantity ELSE 0 END), 0) AS availableQuantity "
                + "FROM product_Ingredient ingredient "
                + "LEFT JOIN inventory_Stock stock "
                + "ON stock.ingredientID = ingredient.ingredientID "
                + "GROUP BY ingredient.ingredientID, ingredient.ingredientName";
        Map<Integer, IngredientStock> stock = new LinkedHashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, stockYear);
            if (locationSpecific) {
                statement.setString(2, warehouseId.trim().toUpperCase());
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    int ingredientId = result.getInt("ingredientID");
                    stock.put(ingredientId, new IngredientStock(
                            result.getString("ingredientName"),
                            result.getInt("availableQuantity")));
                }
            }
        }
        return stock;
    }

    private static Map<Integer, Set<Integer>> loadRequiredIngredients(Connection connection)
            throws SQLException {
        String sql = "SELECT productID, defaultIngredients "
                + "FROM product_DefaultIngredient ORDER BY productID";
        Map<Integer, Set<Integer>> required = new LinkedHashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                int productId = result.getInt("productID");
                Set<Integer> ingredientIds = required.computeIfAbsent(
                        productId, ignored -> new LinkedHashSet<>());
                ingredientIds.addAll(parseIngredientIds(
                        result.getString("defaultIngredients")));
            }
        }
        return required;
    }

    static List<Integer> parseIngredientIds(String encodedIngredients) {
        List<Integer> ingredientIds = new ArrayList<>();
        if (encodedIngredients == null || encodedIngredients.isBlank()) {
            return ingredientIds;
        }

        for (String token : encodedIngredients.split("\\|")) {
            String clean = token.trim();
            int comma = clean.indexOf(',');
            String idText = comma >= 0 ? clean.substring(0, comma).trim() : clean;
            if (idText.isEmpty()) {
                continue;
            }
            try {
                ingredientIds.add(Integer.parseInt(idText));
            } catch (NumberFormatException ignored) {
                // Ignore an invalid legacy token and continue with the remaining recipe.
            }
        }
        return ingredientIds;
    }

    public static final class Availability {
        private final List<String> missingIngredients;
        private final String locationLabel;

        private Availability(List<String> missingIngredients, String locationLabel) {
            this.missingIngredients = Collections.unmodifiableList(
                    new ArrayList<>(missingIngredients));
            this.locationLabel = locationLabel;
        }

        public static Availability available() {
            return new Availability(Collections.emptyList(), null);
        }

        public boolean isAvailable() {
            return missingIngredients.isEmpty();
        }

        public List<String> getMissingIngredients() {
            return missingIngredients;
        }

        public String getLocationLabel() {
            return locationLabel;
        }

        public String getUnavailableMessage() {
            if (isAvailable()) {
                return "";
            }
            String where = locationLabel == null || locationLabel.isBlank()
                    ? "the inventory"
                    : locationLabel;
            return "Unavailable from " + where + ": "
                    + String.join(", ", missingIngredients);
        }
    }

    private static final class IngredientStock {
        private final String name;
        private final int quantity;

        private IngredientStock(String name, int quantity) {
            this.name = name == null || name.isBlank() ? "Unnamed ingredient" : name;
            this.quantity = quantity;
        }
    }
}
