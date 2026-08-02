package ProductPage.ProductPage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SellerProductRepository {

    private static final String DATABASE_URL = "jdbc:sqlite:database/loop.db";

    private SellerProductRepository() {
    }

    public static int saveProduct(SellerProductDraft draft) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            connection.setAutoCommit(false);

            try {
                int productId = insertProduct(connection, draft);
                insertImages(connection, productId, draft);
                insertCategory(connection, productId, draft);
                replaceIngredientOptions(connection, productId, draft);
                replaceDefaultIngredients(connection, productId, draft);
                insertRatingSeed(connection, productId);

                connection.commit();
                return productId;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public static boolean productNameExists(String productName) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        String sql = "SELECT 1 FROM product_Products WHERE LOWER(productName) = LOWER(?) LIMIT 1";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, productName);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public static boolean productNameExistsExcept(String productName, int productId) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        String sql = "SELECT 1 FROM product_Products WHERE LOWER(productName) = LOWER(?) AND productID <> ? LIMIT 1";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, productName);
            statement.setInt(2, productId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public static List<IngredientRecord> loadIngredients() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        String sql = "SELECT ingredientID, ingredientName, calories, protein, carbohydrates, "
            + "sugars, fat, saturatedFat, fiber, sodium FROM product_Ingredient ORDER BY ingredientName";
        List<IngredientRecord> ingredients = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                ingredients.add(new IngredientRecord(
                    result.getInt("ingredientID"),
                    result.getString("ingredientName"),
                    result.getDouble("calories"),
                    result.getDouble("protein"),
                    result.getDouble("carbohydrates"),
                    result.getDouble("sugars"),
                    result.getDouble("fat"),
                    result.getDouble("saturatedFat"),
                    result.getDouble("fiber"),
                    result.getDouble("sodium")
                ));
            }
        }

        return ingredients;
    }

    public static List<ProductIngredientSelection> loadDefaultIngredientsForProduct(int productId) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        List<ProductIngredientSelection> selections = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement optionStatement = connection.prepareStatement(
                 "SELECT optionID, optionGroup, optionName, extraPrice, selectedByDefault, ingredientID "
                     + "FROM product_ProductIngredientOption WHERE productID = ? ORDER BY optionGroup, optionID")) {

            optionStatement.setInt(1, productId);
            try (ResultSet optionResult = optionStatement.executeQuery()) {
                while (optionResult.next()) {
                    int ingredientId = optionResult.getInt("ingredientID");
                    IngredientRecord ingredient = optionResult.wasNull()
                        ? null
                        : loadIngredientById(connection, ingredientId);
                    if (ingredient == null) {
                        ingredient = loadIngredientByName(connection, optionResult.getString("optionName"));
                    }
                    if (ingredient == null) {
                        ingredient = new IngredientRecord(
                            -optionResult.getInt("optionID"),
                            optionResult.getString("optionName"),
                            0, 0, 0, 0, 0, 0, 0, 0
                        );
                    }
                    selections.add(new ProductIngredientSelection(
                        ingredient,
                        optionResult.getDouble("extraPrice"),
                        optionResult.getString("optionGroup"),
                        optionResult.getString("optionName"),
                        optionResult.getInt("selectedByDefault") == 1
                    ));
                }
            }

            if (!selections.isEmpty()) {
                return selections;
            }

            try (PreparedStatement statement = connection.prepareStatement("SELECT defaultIngredients FROM product_DefaultIngredient WHERE productID = ?")) {
                statement.setInt(1, productId);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        String encoded = result.getString("defaultIngredients");
                        for (String token : encoded.split("\\|")) {
                            String[] parts = token.trim().split(",");
                            if (parts.length < 1 || parts[0].isBlank()) {
                                continue;
                            }
                            int ingredientId;
                            try {
                                ingredientId = Integer.parseInt(parts[0].trim());
                            } catch (NumberFormatException exception) {
                                continue;
                            }
                            IngredientRecord ingredient = loadIngredientById(connection, ingredientId);
                            if (ingredient != null) {
                                selections.add(new ProductIngredientSelection(
                                    ingredient,
                                    parts.length > 1 ? parseDouble(parts[1], 0) : 0,
                                    null,
                                    ingredient.name,
                                    true
                                ));
                            }
                        }
                    }
                }
            }
        }

        return selections;
    }

    public static void addIngredient(IngredientRecord ingredient) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        String sql = "INSERT INTO product_Ingredient ("
            + "ingredientName, calories, protein, carbohydrates, sugars, fat, saturatedFat, fiber, sodium"
            + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, ingredient.name);
            statement.setDouble(2, ingredient.calories);
            statement.setDouble(3, ingredient.protein);
            statement.setDouble(4, ingredient.carbohydrates);
            statement.setDouble(5, ingredient.sugars);
            statement.setDouble(6, ingredient.fat);
            statement.setDouble(7, ingredient.saturatedFat);
            statement.setDouble(8, ingredient.fiber);
            statement.setDouble(9, ingredient.sodium);
            statement.executeUpdate();
        }
    }

    private static IngredientRecord loadIngredientById(Connection connection, int ingredientId) throws SQLException {
        String sql = "SELECT ingredientID, ingredientName, calories, protein, carbohydrates, "
            + "sugars, fat, saturatedFat, fiber, sodium FROM product_Ingredient WHERE ingredientID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, ingredientId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return ingredientFromResult(result);
                }
            }
        }
        return null;
    }

    private static IngredientRecord loadIngredientByName(Connection connection, String ingredientName) throws SQLException {
        String sql = "SELECT ingredientID, ingredientName, calories, protein, carbohydrates, "
            + "sugars, fat, saturatedFat, fiber, sodium FROM product_Ingredient WHERE LOWER(ingredientName) = LOWER(?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ingredientName);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return ingredientFromResult(result);
                }
            }
        }
        return null;
    }

    private static IngredientRecord ingredientFromResult(ResultSet result) throws SQLException {
        return new IngredientRecord(
            result.getInt("ingredientID"),
            result.getString("ingredientName"),
            result.getDouble("calories"),
            result.getDouble("protein"),
            result.getDouble("carbohydrates"),
            result.getDouble("sugars"),
            result.getDouble("fat"),
            result.getDouble("saturatedFat"),
            result.getDouble("fiber"),
            result.getDouble("sodium")
        );
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    public static List<ChefRecord> loadChefs() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        String sql = "SELECT chefID, chefName, chefDescription, chefImage FROM product_Chef ORDER BY chefID";
        List<ChefRecord> chefs = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                chefs.add(new ChefRecord(
                    result.getInt("chefID"),
                    result.getString("chefName"),
                    result.getString("chefDescription"),
                    result.getString("chefImage")
                ));
            }
        }

        return chefs;
    }

    public static List<ProductSummary> loadProductSummaries() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        String sql = "SELECT p.productID, p.productName, p.price, p.cost, p.status, p.updatedDate, "
            + "COALESCE(c.chosenDietary, '') AS dietary, "
            + "COALESCE(c.chosenHealthGoal, '') AS healthGoal, "
            + "COALESCE(c.chosenCuisines, '') AS cuisine "
            + "FROM product_Products p "
            + "LEFT JOIN product_Category c ON c.productID = p.productID "
            + "ORDER BY p.updatedDate DESC, p.productID DESC";
        List<ProductSummary> products = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                products.add(new ProductSummary(
                        result.getInt("productID"),
                        result.getString("productName"),
                        result.getString("dietary"),
                        result.getString("healthGoal"),
                        result.getString("cuisine"),
                        result.getDouble("price"),
                        result.getDouble("cost"),
                        result.getInt("status") == 1,
                    result.getString("updatedDate")
                ));
            }
        }

        return products;
    }

    public static ProductEditRecord loadProductForEdit(int productId) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        String sql = "SELECT p.productID, p.productName, p.shortDescription, p.extendedDescription, "
            + "p.cost, p.price, p.status, p.spiceLevel, p.country, p.chefID, "
            + "COALESCE(c.chosenDietary, '') AS dietary, "
            + "COALESCE(c.chosenHealthGoal, '') AS healthGoal, "
            + "COALESCE(c.chosenCuisines, '') AS cuisine "
            + "FROM product_Products p "
            + "LEFT JOIN product_Category c ON c.productID = p.productID "
            + "WHERE p.productID = ?";

        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, productId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new ProductEditRecord(
                        result.getInt("productID"),
                        result.getString("productName"),
                        result.getString("shortDescription"),
                        result.getString("extendedDescription"),
                        result.getDouble("cost"),
                        result.getDouble("price"),
                        result.getInt("status") == 1,
                        result.getInt("spiceLevel"),
                        result.getString("country"),
                        result.getInt("chefID"),
                        result.getString("dietary"),
                        result.getString("healthGoal"),
                        result.getString("cuisine"),
                        loadImagePathsForProduct(connection, productId)
                    );
                }
            }
        }

        throw new SQLException("No product found with productID " + productId + ".");
    }

    private static List<String> loadImagePathsForProduct(Connection connection, int productId) throws SQLException {
        String sql = "SELECT imageURL FROM product_ProductImage WHERE productID = ? ORDER BY displayOrder ASC, imageID ASC";
        List<String> images = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    String imageUrl = result.getString("imageURL");
                    if (imageUrl != null && !imageUrl.isBlank()) {
                        images.add(imageUrl);
                    }
                }
            }
        }

        return images;
    }

    public static void updateProduct(int productId, SellerProductDraft draft) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            connection.setAutoCommit(false);

            try {
                updateProductDetails(connection, productId, draft);
                upsertCategory(connection, productId, draft);
                replaceIngredientOptions(connection, productId, draft);
                replaceDefaultIngredients(connection, productId, draft);
                replaceImages(connection, productId, draft);
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public static void toggleProductStatus(int productId) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        String sql = "UPDATE product_Products SET status = CASE WHEN status = 1 THEN 0 ELSE 1 END, "
            + "updatedDate = datetime('now', 'localtime') WHERE productID = ?";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, productId);
            statement.executeUpdate();
        }
    }

    public static void deleteProduct(int productId) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            connection.setAutoCommit(false);

            try {
                deleteByProductIdIfPresent(connection, "product_ProductImage", productId);
                deleteByProductIdIfPresent(connection, "product_Category", productId);
                deleteByProductIdIfPresent(connection, "product_DefaultIngredient", productId);
                deleteByProductIdIfPresent(connection, "product_Ratings", productId);
                deleteByProductIdIfPresent(connection, "Rating", productId);
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM product_Products WHERE productID = ?")) {
                    statement.setInt(1, productId);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private static void deleteByProductIdIfPresent(Connection connection, String tableName, int productId) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE productID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            if (exception.getMessage() != null
                && exception.getMessage().toLowerCase(Locale.ROOT).contains("no such table")) {
                return;
            }
            throw exception;
        }
    }

    private static int insertProduct(Connection connection, SellerProductDraft draft) throws SQLException {
        String sql = "INSERT INTO product_Products ("
            + "productName, shortDescription, extendedDescription, "
            + "cost, price, status, spiceLevel, country, "
            + "createdDate, updatedDate, chefID"
            + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, draft.productName);
            statement.setString(2, draft.shortDescription);
            statement.setString(3, draft.extendedDescription);
            statement.setDouble(4, draft.cost);
            statement.setDouble(5, draft.price);
            statement.setInt(6, draft.active ? 1 : 0);
            statement.setInt(7, draft.spiceLevel);
            statement.setString(8, draft.country);
            statement.setString(9, draft.timestamp);
            statement.setString(10, draft.timestamp);
            statement.setInt(11, draft.chefId);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("Product was inserted, but no productID was returned.");
    }

    private static void updateProductDetails(Connection connection, int productId, SellerProductDraft draft) throws SQLException {
        String sql = "UPDATE product_Products SET "
            + "productName = ?, shortDescription = ?, extendedDescription = ?, "
            + "cost = ?, price = ?, status = ?, spiceLevel = ?, country = ?, "
            + "updatedDate = ?, chefID = ? "
            + "WHERE productID = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, draft.productName);
            statement.setString(2, draft.shortDescription);
            statement.setString(3, draft.extendedDescription);
            statement.setDouble(4, draft.cost);
            statement.setDouble(5, draft.price);
            statement.setInt(6, draft.active ? 1 : 0);
            statement.setInt(7, draft.spiceLevel);
            statement.setString(8, draft.country);
            statement.setString(9, draft.timestamp);
            statement.setInt(10, draft.chefId);
            statement.setInt(11, productId);
            statement.executeUpdate();
        }
    }

    private static void insertImages(Connection connection, int productId, SellerProductDraft draft) throws SQLException {
        String sql = "INSERT INTO product_ProductImage ("
            + "imageURL, altText, displayOrder, uploadDate, productID"
            + ") VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (SellerImageDraft image : draft.images) {
                statement.setString(1, image.imageUrl);
                statement.setString(2, draft.productName + " image " + image.displayOrder);
                statement.setInt(3, image.displayOrder);
                statement.setString(4, draft.timestamp);
                statement.setInt(5, productId);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void insertCategory(Connection connection, int productId, SellerProductDraft draft) throws SQLException {
        String sql = "INSERT INTO product_Category ("
            + "chosenSortBy, chosenDietary, chosenHealthGoal, chosenCuisines, productID"
            + ") VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "");
            statement.setString(2, draft.dietary);
            statement.setString(3, draft.healthGoal);
            statement.setString(4, draft.cuisine);
            statement.setInt(5, productId);
            statement.executeUpdate();
        }
    }

    private static void upsertCategory(Connection connection, int productId, SellerProductDraft draft) throws SQLException {
        String update = "UPDATE product_Category SET chosenDietary = ?, chosenHealthGoal = ?, chosenCuisines = ? WHERE productID = ?";
        try (PreparedStatement statement = connection.prepareStatement(update)) {
            statement.setString(1, draft.dietary);
            statement.setString(2, draft.healthGoal);
            statement.setString(3, draft.cuisine);
            statement.setInt(4, productId);
            int changed = statement.executeUpdate();
            if (changed > 0) {
                return;
            }
        }

        insertCategory(connection, productId, draft);
    }

    private static void replaceImages(Connection connection, int productId, SellerProductDraft draft) throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement("DELETE FROM product_ProductImage WHERE productID = ?")) {
            delete.setInt(1, productId);
            delete.executeUpdate();
        }
        insertImages(connection, productId, draft);
    }

    private static void replaceDefaultIngredients(Connection connection, int productId, SellerProductDraft draft) throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement("DELETE FROM product_DefaultIngredient WHERE productID = ?")) {
            delete.setInt(1, productId);
            delete.executeUpdate();
        }
        if (draft.ingredients.isEmpty() || draft.ingredients.stream().noneMatch(selection -> selection.selectedByDefault)) {
            return;
        }

        StringBuilder encoded = new StringBuilder();
        double calories = 0;
        double protein = 0;
        double carbohydrates = 0;
        double sugars = 0;
        double fat = 0;
        double saturatedFat = 0;
        double fiber = 0;
        double sodium = 0;
        for (ProductIngredientSelection selection : draft.ingredients) {
            if (!selection.selectedByDefault) {
                continue;
            }
            IngredientRecord ingredient = selection.ingredient;
            encoded.append(ingredient.id)
                .append(',')
                .append(String.format(Locale.ROOT, "%.2f", selection.extraCost))
                .append(" | ");
            calories += ingredient.calories;
            protein += ingredient.protein;
            carbohydrates += ingredient.carbohydrates;
            sugars += ingredient.sugars;
            fat += ingredient.fat;
            saturatedFat += ingredient.saturatedFat;
            fiber += ingredient.fiber;
            sodium += ingredient.sodium;
        }

        String sql = "INSERT INTO product_DefaultIngredient ("
            + "defaultIngredients, totalCalories, totalProtein, totalCarbohydrates, totalSugars, "
            + "totalFat, totalSaturatedFat, totalFiber, totalSodium, productID"
            + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, encoded.toString());
            statement.setDouble(2, calories);
            statement.setDouble(3, protein);
            statement.setDouble(4, carbohydrates);
            statement.setDouble(5, sugars);
            statement.setDouble(6, fat);
            statement.setDouble(7, saturatedFat);
            statement.setDouble(8, fiber);
            statement.setDouble(9, sodium);
            statement.setInt(10, productId);
            statement.executeUpdate();
        }
    }

    private static void replaceIngredientOptions(Connection connection, int productId, SellerProductDraft draft) throws SQLException {
        try (PreparedStatement delete = connection.prepareStatement("DELETE FROM product_ProductIngredientOption WHERE productID = ?")) {
            delete.setInt(1, productId);
            delete.executeUpdate();
        }
        if (draft.ingredients.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO product_ProductIngredientOption ("
            + "productID, ingredientID, optionGroup, optionName, extraPrice, selectedByDefault, allowMultiple, status"
            + ") VALUES (?, ?, ?, ?, ?, ?, ?, 1)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (ProductIngredientSelection selection : draft.ingredients) {
                statement.setInt(1, productId);
                if (selection.ingredient.id > 0) {
                    statement.setInt(2, selection.ingredient.id);
                } else {
                    statement.setNull(2, java.sql.Types.INTEGER);
                }
                statement.setString(3, selection.optionGroup);
                statement.setString(4, selection.optionName);
                statement.setDouble(5, selection.extraCost);
                statement.setInt(6, selection.selectedByDefault ? 1 : 0);
                statement.setInt(7, 1);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static double extraCostForIngredient(String encoded, int ingredientId) {
        if (encoded == null || encoded.isBlank()) {
            return 0;
        }
        String prefix = ingredientId + ",";
        for (String token : encoded.split("\\|")) {
            String clean = token.trim();
            if (clean.startsWith(prefix)) {
                try {
                    return Double.parseDouble(clean.substring(prefix.length()).trim());
                } catch (NumberFormatException exception) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private static void insertRatingSeed(Connection connection, int productId) throws SQLException {
        String sql = "INSERT INTO product_Ratings (rating, noPeople, productID) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, 0);
            statement.setInt(2, 0);
            statement.setInt(3, productId);
            statement.executeUpdate();
        }
    }

    public static final class SellerProductDraft {
        private final String productName;
        private final String shortDescription;
        private final String extendedDescription;
        private final double cost;
        private final double price;
        private final boolean active;
        private final int spiceLevel;
        private final String country;
        private final int chefId;
        private final String dietary;
        private final String healthGoal;
        private final String cuisine;
        private final String timestamp;
        private final List<SellerImageDraft> images;
        private final List<ProductIngredientSelection> ingredients;

        public SellerProductDraft(
                String productName,
                String shortDescription,
                String extendedDescription,
                double cost,
                double price,
                boolean active,
                int spiceLevel,
                String country,
                int chefId,
                String dietary,
                String healthGoal,
                String cuisine,
                List<SellerImageDraft> images,
                List<ProductIngredientSelection> ingredients,
                String timestamp) {

            this.productName = productName;
            this.shortDescription = shortDescription;
            this.extendedDescription = extendedDescription;
            this.cost = cost;
            this.price = price;
            this.active = active;
            this.spiceLevel = spiceLevel;
            this.country = country;
            this.chefId = chefId;
            this.dietary = dietary;
            this.healthGoal = healthGoal;
            this.cuisine = cuisine;
            this.images = images;
            this.ingredients = ingredients;
            this.timestamp = timestamp;
        }
    }

    public static final class ProductIngredientSelection {
        public final IngredientRecord ingredient;
        public final double extraCost;
        public final String optionGroup;
        public final String optionName;
        public final boolean selectedByDefault;

        public ProductIngredientSelection(IngredientRecord ingredient, double extraCost) {
            this(ingredient, extraCost, null, ingredient.name, true);
        }

        public ProductIngredientSelection(
                IngredientRecord ingredient,
                double extraCost,
                String optionGroup,
                String optionName,
                boolean selectedByDefault) {
            this.ingredient = ingredient;
            this.extraCost = extraCost;
            this.optionGroup = optionGroup;
            this.optionName = optionName == null || optionName.isBlank() ? ingredient.name : optionName;
            this.selectedByDefault = selectedByDefault;
        }
    }

    public static final class SellerImageDraft {
        private final int displayOrder;
        private final String imageUrl;

        public SellerImageDraft(int displayOrder, String imageUrl) {
            this.displayOrder = displayOrder;
            this.imageUrl = imageUrl;
        }
    }

    public static final class IngredientRecord {
        public final int id;
        public final String name;
        public final double calories;
        public final double protein;
        public final double carbohydrates;
        public final double sugars;
        public final double fat;
        public final double saturatedFat;
        public final double fiber;
        public final double sodium;

        public IngredientRecord(
                int id,
                String name,
                double calories,
                double protein,
                double carbohydrates,
                double sugars,
                double fat,
                double saturatedFat,
                double fiber,
                double sodium) {

            this.id = id;
            this.name = name;
            this.calories = calories;
            this.protein = protein;
            this.carbohydrates = carbohydrates;
            this.sugars = sugars;
            this.fat = fat;
            this.saturatedFat = saturatedFat;
            this.fiber = fiber;
            this.sodium = sodium;
        }
    }

    public static final class ChefRecord {
        public final int id;
        public final String name;
        public final String description;
        public final String imagePath;

        public ChefRecord(int id, String name, String description, String imagePath) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.imagePath = imagePath;
        }
    }

    public static final class ProductSummary {
        public final int id;
        public final String productName;
        public final String category;
        public final String dietary;
        public final String healthGoal;
        public final String cuisine;
        public final double price;
        public final double cost;
        public final boolean active;
        public final String updatedDate;

        public ProductSummary(int id, String productName, String dietary, String healthGoal, String cuisine, double price, double cost, boolean active, String updatedDate) {
            this.id = id;
            this.productName = productName == null ? "" : productName;
            this.dietary = dietary == null ? "" : dietary;
            this.healthGoal = healthGoal == null ? "" : healthGoal;
            this.cuisine = cuisine == null ? "" : cuisine;
            this.category = this.cuisine.isBlank() ? "Uncategorised" : this.cuisine;
            this.price = price;
            this.cost = cost;
            this.active = active;
            this.updatedDate = updatedDate == null ? "" : updatedDate;
        }
    }

    public static final class ProductEditRecord {
        public final int id;
        public final String productName;
        public final String shortDescription;
        public final String extendedDescription;
        public final double cost;
        public final double price;
        public final boolean active;
        public final int spiceLevel;
        public final String country;
        public final int chefId;
        public final String dietary;
        public final String healthGoal;
        public final String cuisine;
        public final List<String> imageUrls;

        public ProductEditRecord(
                int id,
                String productName,
                String shortDescription,
                String extendedDescription,
                double cost,
                double price,
                boolean active,
                int spiceLevel,
                String country,
                int chefId,
                String dietary,
                String healthGoal,
                String cuisine,
                List<String> imageUrls) {

            this.id = id;
            this.productName = productName == null ? "" : productName;
            this.shortDescription = shortDescription == null ? "" : shortDescription;
            this.extendedDescription = extendedDescription == null ? "" : extendedDescription;
            this.cost = cost;
            this.price = price;
            this.active = active;
            this.spiceLevel = spiceLevel;
            this.country = country == null ? "" : country;
            this.chefId = chefId;
            this.dietary = dietary == null ? "" : dietary;
            this.healthGoal = healthGoal == null ? "" : healthGoal;
            this.cuisine = cuisine == null ? "" : cuisine;
            this.imageUrls = imageUrls == null ? new ArrayList<>() : imageUrls;
        }
    }
}
