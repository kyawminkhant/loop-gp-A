package ProductPage.ProductPage;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class SellerPageController {

    private static final String NO_COUNTRY = "None";
    private static final int PRODUCT_IMAGE_SIZE = 900;
    private static final Path UPLOAD_DIR = Paths.get("product-uploads");
    private static final Path PERMANENT_IMAGE_DIR = Paths.get(
        "src",
        "main",
        "resources",
        "ProductPage",
        "ProductPage",
        "exampleFoods"
    );
    private static final String PERMANENT_IMAGE_PREFIX = "exampleFoods/";
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter DATABASE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ObservableList<String> COUNTRIES = buildCountryList();

    @FXML private ImageView mainPhotoPreview;
    @FXML private Label sellerTitleLabel;
    @FXML private Label mainPhotoPlaceholder;
    @FXML private Label imageUploadStatusLabel;
    @FXML private Label sellerStatusLabel;
    @FXML private Label imageErrorLabel;
    @FXML private Label productNameErrorLabel;
    @FXML private Label shortDescriptionErrorLabel;
    @FXML private Label extendedDescriptionErrorLabel;
    @FXML private Label baseErrorLabel;
    @FXML private Label proteinErrorLabel;
    @FXML private Label addonErrorLabel;
    @FXML private Label spiceErrorLabel;
    @FXML private Label costErrorLabel;
    @FXML private Label priceErrorLabel;
    @FXML private Label countryErrorLabel;
    @FXML private Label chefErrorLabel;
    @FXML private VBox imageOrderList;
    @FXML private TextField productNameField;
    @FXML private TextArea shortDescriptionArea;
    @FXML private TextArea extendedDescriptionArea;
    @FXML private TextField costField;
    @FXML private TextField priceField;
    @FXML private CheckBox statusCheckBox;
    @FXML private ComboBox<String> dietaryComboBox;
    @FXML private ComboBox<String> goalComboBox;
    @FXML private ComboBox<String> cuisineComboBox;
    @FXML private ComboBox<String> countryComboBox;
    @FXML private Spinner<Integer> spiceLevelSpinner;
    @FXML private Button pickBaseButton;
    @FXML private Button pickProteinButton;
    @FXML private Button pickAddonsButton;
    @FXML private Button pickChefButton;
    @FXML private Button saveProductButton;
    @FXML private StackPane sellerOverlay;
    @FXML private VBox ingredientPickerPane;
    @FXML private VBox addIngredientPane;
    @FXML private VBox chefPickerPane;
    @FXML private VBox ingredientOptionList;
    @FXML private VBox chefOptionList;
    @FXML private ScrollPane ingredientListScroll;
    @FXML private ScrollPane chefListScroll;
    @FXML private Label ingredientPickerTitle;
    @FXML private Label ingredientPickerHeading;
    @FXML private Label addIngredientStatusLabel;
    @FXML private Label chefPickerStatusLabel;
    @FXML private CheckBox ingredientSelectedFirstCheckBox;
    @FXML private CheckBox ingredientSelectAllCheckBox;
    @FXML private Button addIngredientBaseButton;
    @FXML private Button addIngredientProteinButton;
    @FXML private Button addIngredientAddonButton;
    @FXML private TextField newIngredientNameField;
    @FXML private TextField newCaloriesField;
    @FXML private TextField newProteinField;
    @FXML private TextField newCarbsField;
    @FXML private TextField newSugarsField;
    @FXML private TextField newFatField;
    @FXML private TextField newSaturatedFatField;
    @FXML private TextField newFiberField;
    @FXML private TextField newSodiumField;

    private final List<UploadedProductImage> productImages = new ArrayList<>();
    private final List<IngredientSelection> selectedBaseIngredients = new ArrayList<>();
    private final List<IngredientSelection> selectedProteinIngredients = new ArrayList<>();
    private final List<IngredientSelection> selectedAddonIngredients = new ArrayList<>();
    private boolean baseSelected;
    private boolean proteinSelected;
    private boolean addonsSelected;
    private boolean chefSelected;
    private static Integer pendingEditProductId;
    private int editingProductId = -1;
    private int selectedChefId = 1;
    private String activeIngredientType = "Base";
    private String newIngredientType = "Base";

    private static ObservableList<String> buildCountryList() {
        TreeSet<String> countries = new TreeSet<>();
        for (String countryCode : Locale.getISOCountries()) {
            String countryName = new Locale("", countryCode).getDisplayCountry(Locale.UK);
            if (countryName != null && !countryName.isBlank() && hasFlagForCountryCode(countryCode)) {
                countries.add(countryName);
            }
        }
        ObservableList<String> countryList = FXCollections.observableArrayList(NO_COUNTRY);
        countryList.addAll(countries);
        return countryList;
    }

    private static boolean hasFlagForCountryCode(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return false;
        }
        String path = "/ProductPage/ProductPage/world_flags/" + countryCode.toLowerCase(Locale.ENGLISH) + ".png";
        return SellerPageController.class.getResource(path) != null;
    }

    @FXML
    private void initialize() {
        spiceLevelSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0));

        dietaryComboBox.setItems(FXCollections.observableArrayList(
                "Vegetarian", "Vegan", "Halal", "Gluten-Free", "Pescatarian"));
        goalComboBox.setItems(FXCollections.observableArrayList(
                "Weight Loss", "High Protein", "Balanced Meals"));
        cuisineComboBox.setItems(FXCollections.observableArrayList(
                "Asian", "Southeast Asian", "South Asian", "Middle Eastern", "Western", "Mediterranean"));
        installSearchableCountryComboBox();
        countryComboBox.setOnAction(event -> updateCuisineFromCountry());

        dietaryComboBox.getSelectionModel().select("Vegetarian");
        goalComboBox.getSelectionModel().select("Balanced Meals");
        cuisineComboBox.getSelectionModel().select("Asian");
        countryComboBox.getSelectionModel().select(NO_COUNTRY);
        loadPendingEditProduct();
    }

    public static void prepareEditProduct(int productId) {
        pendingEditProductId = productId;
    }

    public static void prepareAddProduct() {
        pendingEditProductId = null;
    }

    private void loadPendingEditProduct() {
        if (pendingEditProductId == null) {
            return;
        }

        int productId = pendingEditProductId;
        pendingEditProductId = null;
        try {
            startEditingProduct(productId);
        } catch (ClassNotFoundException | SQLException exception) {
            exception.printStackTrace();
            sellerStatusLabel.setText("Could not load this product for editing.");
        }
    }

    public void startEditingProduct(int productId) throws ClassNotFoundException, SQLException {
        SellerProductRepository.ProductEditRecord product = SellerProductRepository.loadProductForEdit(productId);
        editingProductId = product.id;
        clearErrors();
        sellerTitleLabel.setText("Modify Product");
        if (saveProductButton != null) {
            saveProductButton.setText("Update Product");
        }

        productNameField.setText(product.productName);
        shortDescriptionArea.setText(product.shortDescription);
        extendedDescriptionArea.setText(product.extendedDescription);
        costField.setText(String.format("£%.2f", product.cost));
        priceField.setText(String.format("£%.2f", product.price));
        statusCheckBox.setSelected(product.active);
        spiceLevelSpinner.getValueFactory().setValue(Math.max(0, Math.min(5, product.spiceLevel)));

        selectComboValue(dietaryComboBox, product.dietary);
        selectComboValue(goalComboBox, product.healthGoal);
        selectComboValue(cuisineComboBox, product.cuisine);
        selectComboValue(countryComboBox, product.country);

        selectedChefId = product.chefId;
        chefSelected = true;
        pickChefButton.setText("Chef " + selectedChefId + " Selected");

        baseSelected = true;
        proteinSelected = true;
        addonsSelected = true;
        pickBaseButton.setText("Base Selected");
        pickProteinButton.setText("Protein Selected");
        pickAddonsButton.setText("Add-ons Selected");

        productImages.clear();
        imageOrderList.getChildren().clear();
        for (int i = 0; i < product.imageUrls.size(); i++) {
            productImages.add(new UploadedProductImage(i + 1, resolveImagePath(product.imageUrls.get(i))));
        }
        refreshImageOrders();
        updateMainPreview();
        imageErrorLabel.setVisible(false);
        imageErrorLabel.setManaged(false);
        imageUploadStatusLabel.setText(productImages.isEmpty()
                ? "Editing existing product. Upload PNGs to add product photos."
                : "Loaded " + productImages.size() + " existing image(s). Upload PNGs only if you want to add or replace images.");
        loadExistingIngredientSelections(product);
        sellerStatusLabel.setText("Editing productID " + editingProductId + ". Save Product will update this item.");
    }

    @FXML
    private void backToProductManager() {
        try {
            App.setRoot("Product Manager");
        } catch (IOException exception) {
            exception.printStackTrace();
            sellerStatusLabel.setText("Could not go back to Product Manager.");
        }
    }

    @FXML
    private void uploadProductImages() {
        List<UploadedProductImage> uploaded = chooseAndFormatPngs();
        if (!uploaded.isEmpty()) {
            productImages.addAll(uploaded);
            refreshImageOrders();
            updateMainPreview();
            imageUploadStatusLabel.setText(productImages.size() + " formatted PNG image(s). Use Up/Down to set order.");
            sellerStatusLabel.setText("Images formatted and ordered. Order 1 is the main product photo.");
        }
    }

    @FXML
    private void discardForm() {
        editingProductId = -1;
        sellerTitleLabel.setText("Add New Product");
        clearErrors();
        productNameField.clear();
        shortDescriptionArea.clear();
        extendedDescriptionArea.clear();
        costField.clear();
        priceField.clear();
        statusCheckBox.setSelected(true);
        mainPhotoPreview.setImage(null);
        mainPhotoPlaceholder.setVisible(true);
        imageUploadStatusLabel.setText("Select multiple .png files. First image becomes main photo.");
        productImages.clear();
        imageOrderList.getChildren().clear();
        resetPickerButtons();
        sellerStatusLabel.setText("Form discarded.");
    }

    @FXML
    private void saveProduct() {
        clearErrors();
        boolean valid = true;

        String productName = text(productNameField.getText());
        if (productImages.isEmpty() && editingProductId < 0) {
            setError(imageErrorLabel, "Upload at least one PNG image. The first uploaded image becomes the main photo.");
            valid = false;
        }
        if (productName.isBlank()) {
            setError(productNameErrorLabel, "Enter a product name before saving.");
            valid = false;
        } else if (productName.length() < 3) {
            setError(productNameErrorLabel, "Product name must be at least 3 characters.");
            valid = false;
        } else {
            try {
                boolean duplicateName = editingProductId > 0
                    ? SellerProductRepository.productNameExistsExcept(productName, editingProductId)
                    : SellerProductRepository.productNameExists(productName);
                if (duplicateName) {
                    setError(productNameErrorLabel, "A product with this name already exists.");
                    valid = false;
                }
            } catch (ClassNotFoundException | SQLException exception) {
                exception.printStackTrace();
                setError(productNameErrorLabel, "Could not check product name in the database.");
                valid = false;
            }
        }

        int shortWords = wordCount(shortDescriptionArea.getText());
        if (shortWords < 30 || shortWords > 50) {
            setError(shortDescriptionErrorLabel, "Short description should be 30 to 50 words. Current: " + shortWords + ".");
            valid = false;
        }

        int extendedWords = wordCount(extendedDescriptionArea.getText());
        if (extendedWords < 80) {
            setError(extendedDescriptionErrorLabel, "Extended description should be at least 80 words. Current: " + extendedWords + ".");
            valid = false;
        }

        if (!baseSelected) {
            setError(baseErrorLabel, "Choose a base ingredient.");
            valid = false;
        } else if (!hasDefaultSelection(selectedBaseIngredients)) {
            setError(baseErrorLabel, "Set at least one default base.");
            valid = false;
        }
        if (!proteinSelected) {
            setError(proteinErrorLabel, "Choose a protein ingredient.");
            valid = false;
        } else if (!hasDefaultSelection(selectedProteinIngredients)) {
            setError(proteinErrorLabel, "Set at least one default protein.");
            valid = false;
        }
        if (!addonsSelected) {
            setWarning(addonErrorLabel, "Optional. Add-ons can be skipped.");
        }
        if (!chefSelected) {
            setError(chefErrorLabel, "Choose the chef for this product.");
            valid = false;
        }

        Integer spiceLevel = spiceLevelSpinner.getValue();
        if (spiceLevel == null || spiceLevel < 0 || spiceLevel > 5) {
            setError(spiceErrorLabel, "Spice level must be between 0 and 5.");
            valid = false;
        }

        Double cost = parseMoney(costField.getText());
        Double price = parseMoney(priceField.getText());
        if (cost == null || cost < 0) {
            setError(costErrorLabel, "Cost is invalid. Enter a number 0 or higher, for example 5.40.");
            valid = false;
        }
        if (price == null || price <= 0) {
            setError(priceErrorLabel, "Price is invalid. Enter a number greater than 0, for example 9.99.");
            valid = false;
        } else if (cost != null && price <= cost) {
            setError(priceErrorLabel, "Price must be higher than the cost.");
            valid = false;
        }

        if (!hasSelectedCountry()) {
            setError(countryErrorLabel, "Choose a country for this product.");
            if (countryErrorLabel == null) {
                sellerStatusLabel.setText("Choose a country for this product.");
            }
            valid = false;
        }

        if (!valid) {
            sellerStatusLabel.setText("Please fix the highlighted fields before saving.");
            return;
        }

        try {
            SellerProductRepository.SellerProductDraft draft = buildProductDraft(
                productName,
                cost,
                price,
                spiceLevel
            );
            boolean editing = editingProductId > 0;
            int productId;
            if (editing) {
                SellerProductRepository.updateProduct(editingProductId, draft);
                productId = editingProductId;
            } else {
                productId = SellerProductRepository.saveProduct(draft);
            }

            sellerStatusLabel.setText(
                editing
                    ? "Product updated in database with productID " + productId + "."
                    : "Product saved to database with productID " + productId + "."
            );
            editingProductId = -1;
            if (editing) {
                try {
                    App.setRoot("Product Manager");
                } catch (IOException exception) {
                    exception.printStackTrace();
                    sellerStatusLabel.setText("Product updated, but could not return to Product Manager.");
                }
                return;
            }
            discardSavedForm();
        } catch (ClassNotFoundException | SQLException exception) {
            exception.printStackTrace();
            sellerStatusLabel.setText(
                "Database save failed. Check that database/loop.db exists and the table columns match."
            );
        }
    }

    @FXML
    private void pickBase() {
        openIngredientPicker("Base");
    }

    @FXML
    private void pickProtein() {
        openIngredientPicker("Protein");
    }

    @FXML
    private void pickAddons() {
        openIngredientPicker("Add-On");
    }

    @FXML
    private void pickChef() {
        openChefPicker();
    }

    @FXML
    private void closePickerPane() {
        sellerOverlay.setVisible(false);
        sellerOverlay.setManaged(false);
        ingredientPickerPane.setVisible(false);
        ingredientPickerPane.setManaged(false);
        addIngredientPane.setVisible(false);
        addIngredientPane.setManaged(false);
        chefPickerPane.setVisible(false);
        chefPickerPane.setManaged(false);
    }

    @FXML
    private void backToIngredientPicker() {
        openIngredientPicker(activeIngredientType);
    }

    @FXML
    private void toggleAllIngredients() {
        boolean selected = ingredientSelectAllCheckBox.isSelected();
        for (Node node : ingredientOptionList.getChildren()) {
            CheckBox box = (CheckBox) node.lookup(".ingredient-row-check");
            if (box != null) {
                box.setSelected(selected);
            }
        }
        sortIngredientRowsIfRequested();
    }

    @FXML
    private void sortIngredientPickerSelectedFirst() {
        sortIngredientRowsIfRequested();
    }

    @FXML
    private void confirmIngredientSelection() {
        List<IngredientSelection> target = selectionsFor(activeIngredientType);
        target.clear();

        for (Node node : ingredientOptionList.getChildren()) {
            CheckBox checkBox = (CheckBox) node.lookup(".ingredient-row-check");
            TextField costField = (TextField) node.lookup(".ingredient-cost-field");
            Button defaultButton = (Button) node.lookup(".ingredient-default-button");
            Object data = node.getUserData();

            if (checkBox != null
                    && checkBox.isSelected()
                    && data instanceof SellerProductRepository.IngredientRecord) {

                SellerProductRepository.IngredientRecord ingredient =
                    (SellerProductRepository.IngredientRecord) data;
                target.add(new IngredientSelection(
                    ingredient,
                    parseOptionalMoney(costField == null ? "" : costField.getText()),
                    activeIngredientType,
                    ingredient.name,
                    !"Add-On".equals(activeIngredientType) && defaultButton != null && "Default".equals(defaultButton.getText())
                ));
            }
        }

        if (!"Add-On".equals(activeIngredientType)
                && target.stream().noneMatch(selection -> selection.selectedByDefault)) {
            sellerStatusLabel.setText("Choose at least one default " + activeIngredientType.toLowerCase() + " option.");
            return;
        }

        updateIngredientButton(activeIngredientType);
        closePickerPane();
        sellerStatusLabel.setText(activeIngredientType + " selection updated.");
    }

    @FXML
    private void openAddIngredientPane() {
        newIngredientType = activeIngredientType;
        updateNewIngredientTypeButtons();
        addIngredientStatusLabel.setVisible(false);
        addIngredientStatusLabel.setManaged(false);
        showOverlayPane(addIngredientPane);
    }

    @FXML
    private void selectNewIngredientBase() {
        newIngredientType = "Base";
        updateNewIngredientTypeButtons();
    }

    @FXML
    private void selectNewIngredientProtein() {
        newIngredientType = "Protein";
        updateNewIngredientTypeButtons();
    }

    @FXML
    private void selectNewIngredientAddon() {
        newIngredientType = "Add-On";
        updateNewIngredientTypeButtons();
    }

    @FXML
    private void addNewIngredient() {
        String name = text(newIngredientNameField.getText());
        if (name.isBlank()) {
            showPickerError(addIngredientStatusLabel, "Enter an ingredient name first.");
            return;
        }

        Double calories = parseNumber(newCaloriesField.getText());
        Double protein = parseNumber(newProteinField.getText());
        Double carbs = parseNumber(newCarbsField.getText());
        Double sugars = parseNumber(newSugarsField.getText());
        Double fat = parseNumber(newFatField.getText());
        Double saturatedFat = parseNumber(newSaturatedFatField.getText());
        Double fiber = parseNumber(newFiberField.getText());
        Double sodium = parseNumber(newSodiumField.getText());

        if (Arrays.asList(calories, protein, carbs, sugars, fat, saturatedFat, fiber, sodium).contains(null)) {
            showPickerError(addIngredientStatusLabel, "Nutrition values must be valid numbers.");
            return;
        }

        try {
            SellerProductRepository.addIngredient(new SellerProductRepository.IngredientRecord(
                0, name, calories, protein, carbs, sugars, fat, saturatedFat, fiber, sodium
            ));
            clearNewIngredientForm();
            activeIngredientType = newIngredientType;
            openIngredientPicker(activeIngredientType);
            sellerStatusLabel.setText(name + " added to the ingredient database.");
        } catch (ClassNotFoundException | SQLException exception) {
            exception.printStackTrace();
            showPickerError(addIngredientStatusLabel, "Could not add ingredient to the database.");
        }
    }

    @FXML
    private void decreaseSpiceLevel() {
        int value = spiceLevelSpinner.getValue() == null ? 0 : spiceLevelSpinner.getValue();
        spiceLevelSpinner.getValueFactory().setValue(Math.max(0, value - 1));
        clearError(spiceErrorLabel);
    }

    @FXML
    private void increaseSpiceLevel() {
        int value = spiceLevelSpinner.getValue() == null ? 0 : spiceLevelSpinner.getValue();
        spiceLevelSpinner.getValueFactory().setValue(Math.min(5, value + 1));
        clearError(spiceErrorLabel);
    }

    private List<UploadedProductImage> chooseAndFormatPngs() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose PNG product images");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG images only", "*.png"));

        Window owner = sellerStatusLabel.getScene() == null ? null : sellerStatusLabel.getScene().getWindow();
        List<File> selectedFiles = chooser.showOpenMultipleDialog(owner);
        List<UploadedProductImage> uploadedImages = new ArrayList<>();

        if (selectedFiles == null || selectedFiles.isEmpty()) {
            return uploadedImages;
        }

        try {
            Files.createDirectories(UPLOAD_DIR);

            for (File selected : selectedFiles) {
                if (!selected.getName().toLowerCase().endsWith(".png")) {
                    sellerStatusLabel.setText("Skipped non-PNG file: " + selected.getName());
                    continue;
                }

                String safeName = safeFilePart(productNameField.getText());
                int order = productImages.size() + uploadedImages.size() + 1;
                String outputName = safeName + "-image-" + order + "-" + FILE_TIME.format(LocalDateTime.now()) + ".png";
                Path output = UPLOAD_DIR.resolve(outputName);

                BufferedImage original = ImageIO.read(selected);
                if (original == null) {
                    sellerStatusLabel.setText("Could not read PNG image: " + selected.getName());
                    continue;
                }

                BufferedImage formatted = centerCropSquare(original, PRODUCT_IMAGE_SIZE);
                ImageIO.write(formatted, "png", output.toFile());
                uploadedImages.add(new UploadedProductImage(order, output));
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            sellerStatusLabel.setText("Image formatting failed. Check console for details.");
        }

        return uploadedImages;
    }

    private BufferedImage centerCropSquare(BufferedImage source, int size) {
        int cropSize = Math.min(source.getWidth(), source.getHeight());
        int cropX = (source.getWidth() - cropSize) / 2;
        int cropY = (source.getHeight() - cropSize) / 2;

        BufferedImage target = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = target.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(source, 0, 0, size, size, cropX, cropY, cropX + cropSize, cropY + cropSize, null);
        graphics.dispose();
        return target;
    }

    private void showPreview(ImageView preview, Label placeholder, Path imagePath) {
        if (imagePath == null || !Files.exists(imagePath)) {
            preview.setImage(null);
            placeholder.setVisible(true);
            return;
        }
        preview.setImage(new Image(imagePath.toUri().toString(), 170, 170, false, true));
        placeholder.setVisible(false);
    }

    private Path resolveImagePath(String imageUrl) {
        String clean = text(imageUrl);
        if (clean.isBlank()) {
            return Paths.get("");
        }

        Path stored = Paths.get(clean);
        if (Files.exists(stored)) {
            return stored;
        }

        Path projectRelative = Paths.get(System.getProperty("user.dir")).resolve(clean).normalize();
        if (Files.exists(projectRelative)) {
            return projectRelative;
        }

        Path resourceRelative = Paths.get(System.getProperty("user.dir"))
            .resolve("src/main/resources/ProductPage/ProductPage")
            .resolve(clean)
            .normalize();
        if (Files.exists(resourceRelative)) {
            return resourceRelative;
        }

        return stored;
    }

    private void loadExistingIngredientSelections(SellerProductRepository.ProductEditRecord product) throws ClassNotFoundException, SQLException {
        selectedBaseIngredients.clear();
        selectedProteinIngredients.clear();
        selectedAddonIngredients.clear();
        for (SellerProductRepository.ProductIngredientSelection selection
                : SellerProductRepository.loadDefaultIngredientsForProduct(product.id)) {
            String group = selection.optionGroup == null ? null : selection.optionGroup;
            IngredientSelection localSelection = new IngredientSelection(
                selection.ingredient,
                selection.extraCost,
                group,
                selection.optionName,
                selection.selectedByDefault
            );
            if ("Base".equals(group) || (group == null && matchesIngredientType(selection.ingredient.name, "Base"))) {
                selectedBaseIngredients.add(localSelection);
            } else if ("Protein".equals(group) || (group == null && matchesIngredientType(selection.ingredient.name, "Protein"))) {
                selectedProteinIngredients.add(localSelection);
            } else {
                selectedAddonIngredients.add(localSelection);
            }
        }
        if (selectedAddonIngredients.isEmpty()) {
            selectedAddonIngredients.addAll(fallbackAddOnsForProduct(product));
        }
        updateIngredientButton("Base");
        updateIngredientButton("Protein");
        updateIngredientButton("Add-On");
    }

    private List<IngredientSelection> fallbackAddOnsForProduct(SellerProductRepository.ProductEditRecord product) {
        List<IngredientSelection> addOns = new ArrayList<>();
        String details =
            product.productName + " "
                + product.dietary + " "
                + product.healthGoal + " "
                + product.cuisine + " "
                + product.country + " "
                + product.shortDescription + " "
                + product.extendedDescription;

        for (ProductIngredientOptionRules.Option option : ProductIngredientOptionRules.fallbackAddOns(details)) {
            addFallbackAddOn(addOns, option.name, option.extraPrice);
        }
        return addOns;
    }

    private void addFallbackAddOn(List<IngredientSelection> addOns, String name, double extraCost) {
        SellerProductRepository.IngredientRecord ingredient =
            new SellerProductRepository.IngredientRecord(-Math.abs(name.toLowerCase().hashCode()), name, 0, 0, 0, 0, 0, 0, 0, 0);
        addOns.add(new IngredientSelection(ingredient, extraCost, "Add-On", name, false));
    }

    private void refreshImageOrders() {
        imageOrderList.getChildren().clear();
        for (int i = 0; i < productImages.size(); i++) {
            UploadedProductImage image = productImages.get(i);
            image.order = i + 1;

            Label orderLabel = new Label("Order " + image.order);
            orderLabel.getStyleClass().add("seller-image-order-label");

            Label fileLabel = new Label(image.outputPath.getFileName().toString());
            fileLabel.getStyleClass().add("seller-image-file-label");
            fileLabel.setMaxWidth(Double.MAX_VALUE);
            fileLabel.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
            fileLabel.setTooltip(new Tooltip(image.outputPath.getFileName().toString()));

            Button upButton = new Button("Move Up");
            upButton.getStyleClass().add("seller-order-button");
            int index = i;
            upButton.setOnAction(event -> moveImage(index, -1));

            Button downButton = new Button("Move Down");
            downButton.getStyleClass().add("seller-order-button");
            downButton.setOnAction(event -> moveImage(index, 1));
            upButton.setDisable(index == 0);
            downButton.setDisable(index == productImages.size() - 1);

            Button removeButton = new Button("Remove");
            removeButton.getStyleClass().addAll("seller-order-button", "seller-remove-image-button");
            removeButton.setOnAction(event -> removeImage(index));

            HBox row = new HBox(10, orderLabel, fileLabel, upButton, downButton, removeButton);
            row.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(fileLabel, Priority.ALWAYS);
            row.getStyleClass().add("seller-image-order-row");
            imageOrderList.getChildren().add(row);
        }
    }

    private void moveImage(int index, int direction) {
        int target = index + direction;
        if (target < 0 || target >= productImages.size()) {
            return;
        }

        UploadedProductImage image = productImages.remove(index);
        productImages.add(target, image);
        refreshImageOrders();
        updateMainPreview();
        sellerStatusLabel.setText("Image order updated. Order 1 is the main photo.");
    }

    private void removeImage(int index) {
        if (index < 0 || index >= productImages.size()) {
            return;
        }

        productImages.remove(index);
        refreshImageOrders();
        updateMainPreview();
        imageUploadStatusLabel.setText(productImages.isEmpty()
            ? "No product images selected."
            : productImages.size() + " PNG image(s) selected.");
        sellerStatusLabel.setText("Image removed. Save the product to keep this change.");
    }

    private void updateMainPreview() {
        if (productImages.isEmpty()) {
            mainPhotoPreview.setImage(null);
            mainPhotoPlaceholder.setVisible(true);
            return;
        }

        showPreview(mainPhotoPreview, mainPhotoPlaceholder, productImages.get(0).outputPath);
    }

    private void openIngredientPicker(String type) {
        activeIngredientType = type;
        ingredientPickerTitle.setText("Set " + ("Add-On".equals(type) ? "Add-ons" : type + "s"));
        ingredientPickerHeading.setText("Choose " + ("Add-On".equals(type) ? "Add-on" : type) + " Options");
        ingredientSelectAllCheckBox.setSelected(false);
        ingredientOptionList.getChildren().clear();
        if (ingredientListScroll != null) {
            ingredientListScroll.setVvalue(0);
        }

        try {
            Set<String> shownIngredients = new HashSet<>();
            for (IngredientSelection selection : selectionsFor(type)) {
                ingredientOptionList.getChildren().add(createIngredientRow(selection.ingredient, type));
                shownIngredients.add(ingredientKey(selection.ingredient, selection.optionName));
            }

            List<SellerProductRepository.IngredientRecord> ingredients =
                SellerProductRepository.loadIngredients();
            for (SellerProductRepository.IngredientRecord ingredient : ingredients) {
                if (matchesIngredientType(ingredient.name, type)
                        && !shownIngredients.contains(ingredientKey(ingredient, ingredient.name))) {
                    ingredientOptionList.getChildren().add(createIngredientRow(ingredient, type));
                }
            }
            sortIngredientRowsIfRequested();

            if (ingredientOptionList.getChildren().isEmpty()) {
                Label empty = new Label("No " + type.toLowerCase() + " ingredients found in the database.");
                empty.getStyleClass().add("seller-help-text");
                ingredientOptionList.getChildren().add(empty);
            }

            showOverlayPane(ingredientPickerPane);
        } catch (ClassNotFoundException | SQLException exception) {
            exception.printStackTrace();
            sellerStatusLabel.setText("Could not load ingredients from the database.");
        }
    }

    private GridPane createIngredientRow(SellerProductRepository.IngredientRecord ingredient, String type) {
        GridPane row = new GridPane();
        row.setHgap(12);
        row.getStyleClass().add("ingredient-picker-row");
        row.setUserData(ingredient);
        row.getColumnConstraints().setAll(ingredientRowColumns("Add-On".equals(type)));

        CheckBox checkBox = new CheckBox();
        checkBox.getStyleClass().add("ingredient-row-check");
        IngredientSelection existingSelection = existingIngredientSelection(ingredient.id, type);
        checkBox.setSelected(existingSelection != null);
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) ->
            Platform.runLater(this::sortIngredientRowsIfRequested)
        );

        Label name = new Label(ingredient.name);
        name.getStyleClass().add("ingredient-row-name");
        name.setMaxWidth(Double.MAX_VALUE);
        name.setTextOverrun(OverrunStyle.ELLIPSIS);
        name.setTooltip(new Tooltip(ingredient.name));

        Label costLabel = new Label("Extra Cost:");
        costLabel.getStyleClass().add("ingredient-field-label");

        TextField extraCost = new TextField(formatExtraCost(existingSelection == null ? 0 : existingSelection.extraCost));
        extraCost.getStyleClass().add("ingredient-cost-field");
        extraCost.setMaxWidth(Double.MAX_VALUE);

        Button defaultButton = null;
        if (!"Add-On".equals(type)) {
            defaultButton = new Button(existingSelection != null && existingSelection.selectedByDefault ? "Default" : "Set Default");
            defaultButton.getStyleClass().add("ingredient-default-button");
            Button defaultToggle = defaultButton;
            defaultButton.setOnAction(event -> {
                boolean makeDefault = !"Default".equals(defaultToggle.getText());
                checkBox.setSelected(makeDefault || checkBox.isSelected());
                defaultToggle.setText(makeDefault ? "Default" : "Set Default");
            });
        }

        row.add(checkBox, 0, 0);
        row.add(name, 1, 0);
        row.add(costLabel, 2, 0);
        row.add(extraCost, 3, 0);
        if (defaultButton != null) {
            row.add(defaultButton, 4, 0);
        }
        return row;
    }

    private List<ColumnConstraints> ingredientRowColumns(boolean addOnRow) {
        List<ColumnConstraints> columns = new ArrayList<>();
        columns.add(fixedColumn(48));
        columns.add(fixedColumn(addOnRow ? 430 : 330));
        columns.add(fixedColumn(120));
        columns.add(fixedColumn(140));
        if (!addOnRow) {
            columns.add(fixedColumn(170));
        }
        return columns;
    }

    private ColumnConstraints fixedColumn(double width) {
        ColumnConstraints column = new ColumnConstraints();
        column.setMinWidth(width);
        column.setPrefWidth(width);
        column.setMaxWidth(width);
        return column;
    }

    private void sortIngredientRowsIfRequested() {
        if (ingredientSelectedFirstCheckBox == null || !ingredientSelectedFirstCheckBox.isSelected()) {
            return;
        }
        List<Node> sortedRows = new ArrayList<>(ingredientOptionList.getChildren());
        sortedRows.sort(
            Comparator
                .comparing((Node node) -> !isIngredientRowSelected(node))
                .thenComparing(this::ingredientRowName, String.CASE_INSENSITIVE_ORDER)
        );
        ingredientOptionList.getChildren().setAll(sortedRows);
    }

    private boolean isIngredientRowSelected(Node node) {
        CheckBox box = (CheckBox) node.lookup(".ingredient-row-check");
        return box != null && box.isSelected();
    }

    private String ingredientRowName(Node node) {
        Object data = node.getUserData();
        if (data instanceof SellerProductRepository.IngredientRecord) {
            return ((SellerProductRepository.IngredientRecord) data).name;
        }
        return "";
    }

    private void openChefPicker() {
        chefOptionList.getChildren().clear();
        if (chefListScroll != null) {
            chefListScroll.setVvalue(0);
        }
        try {
            for (SellerProductRepository.ChefRecord chef : SellerProductRepository.loadChefs()) {
                HBox row = new HBox(16);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("chef-picker-row");

                Label id = new Label("#" + chef.id);
                id.getStyleClass().add("chef-picker-id");

                Label name = new Label(chef.name);
                name.getStyleClass().add("chef-picker-name");
                name.setTextOverrun(OverrunStyle.CLIP);

                Label description = new Label(chef.description);
                description.getStyleClass().add("chef-picker-description");
                description.setWrapText(true);
                description.setMinWidth(260);
                description.setMaxWidth(420);
                description.setTooltip(new Tooltip(chef.description));

                Button select = new Button("Select");
                select.getStyleClass().addAll("seller-small-button", "product_Chef-select-button");
                select.setMinWidth(132);
                select.setPrefWidth(132);
                select.setMaxWidth(132);
                select.setOnAction(event -> selectChef(chef));

                row.getChildren().addAll(id, name, description, select);
                HBox.setHgrow(description, Priority.ALWAYS);
                chefOptionList.getChildren().add(row);
            }

            showOverlayPane(chefPickerPane);
        } catch (ClassNotFoundException | SQLException exception) {
            exception.printStackTrace();
            sellerStatusLabel.setText("Could not load chefs from the database.");
        }
    }

    private void selectChef(SellerProductRepository.ChefRecord chef) {
        chefSelected = true;
        selectedChefId = chef.id;
        pickChefButton.setText("Chef " + chef.id + " Selected");
        pickChefButton.setTooltip(new Tooltip(chef.name));
        clearError(chefErrorLabel);
        closePickerPane();
        sellerStatusLabel.setText("Chef selected: " + chef.name + ".");
    }

    private void showOverlayPane(VBox pane) {
        sellerOverlay.setVisible(true);
        sellerOverlay.setManaged(true);

        ingredientPickerPane.setVisible(false);
        ingredientPickerPane.setManaged(false);
        addIngredientPane.setVisible(false);
        addIngredientPane.setManaged(false);
        chefPickerPane.setVisible(false);
        chefPickerPane.setManaged(false);

        pane.setVisible(true);
        pane.setManaged(true);
    }

    private List<IngredientSelection> selectionsFor(String type) {
        if ("Base".equals(type)) {
            return selectedBaseIngredients;
        }
        if ("Protein".equals(type)) {
            return selectedProteinIngredients;
        }
        return selectedAddonIngredients;
    }

    private void updateIngredientButton(String type) {
        if ("Base".equals(type)) {
            baseSelected = !selectedBaseIngredients.isEmpty();
            pickBaseButton.setText(baseSelected ? "Base (" + selectedBaseIngredients.size() + ")" : "Pick Base");
            clearError(baseErrorLabel);
        } else if ("Protein".equals(type)) {
            proteinSelected = !selectedProteinIngredients.isEmpty();
            pickProteinButton.setText(proteinSelected ? "Protein (" + selectedProteinIngredients.size() + ")" : "Pick Protein");
            clearError(proteinErrorLabel);
        } else {
            addonsSelected = !selectedAddonIngredients.isEmpty();
            pickAddonsButton.setText(addonsSelected ? "Add-ons (" + selectedAddonIngredients.size() + ")" : "Pick Add-ons");
            clearError(addonErrorLabel);
        }
    }

    private boolean isIngredientAlreadySelected(int ingredientId, String type) {
        for (IngredientSelection selection : selectionsFor(type)) {
            if (selection.ingredient.id == ingredientId) {
                return true;
            }
        }
        return false;
    }

    private String ingredientKey(SellerProductRepository.IngredientRecord ingredient, String optionName) {
        if (ingredient.id > 0) {
            return "id:" + ingredient.id;
        }
        String name = optionName == null || optionName.isBlank() ? ingredient.name : optionName;
        return "name:" + name.trim().toLowerCase();
    }

    private IngredientSelection existingIngredientSelection(int ingredientId, String type) {
        for (IngredientSelection selection : selectionsFor(type)) {
            if (selection.ingredient.id == ingredientId) {
                return selection;
            }
        }
        return null;
    }

    private String formatExtraCost(double value) {
        return String.format("£%.2f", value);
    }

    private List<SellerProductRepository.ProductIngredientSelection> productIngredientDrafts() {
        List<SellerProductRepository.ProductIngredientSelection> selections = new ArrayList<>();
        addIngredientDrafts(selections, selectedBaseIngredients, "Base");
        addIngredientDrafts(selections, selectedProteinIngredients, "Protein");
        addIngredientDrafts(selections, selectedAddonIngredients, "Add-On");
        return selections;
    }

    private void addIngredientDrafts(
            List<SellerProductRepository.ProductIngredientSelection> target,
            List<IngredientSelection> source,
            String fallbackGroup) {
        for (IngredientSelection selection : source) {
            target.add(new SellerProductRepository.ProductIngredientSelection(
                selection.ingredient,
                selection.extraCost,
                selection.optionGroup == null ? fallbackGroup : selection.optionGroup,
                selection.optionName,
                selection.selectedByDefault
            ));
        }
    }

    private boolean hasDefaultSelection(List<IngredientSelection> selections) {
        for (IngredientSelection selection : selections) {
            if (selection.selectedByDefault) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesIngredientType(String ingredientName, String type) {
        String name = ingredientName == null ? "" : ingredientName.toLowerCase();
        boolean base = containsAny(name, "rice", "quinoa", "noodle", "pasta", "bread", "bun", "wrap", "flatbread", "potato");
        boolean protein = containsAny(name, "egg", "tofu", "chicken", "beef", "salmon", "shrimp", "tuna", "lamb", "pork", "fish", "turkey", "falafel", "hummus");

        if ("Base".equals(type)) {
            return base;
        }
        if ("Protein".equals(type)) {
            return protein;
        }
        return !base && !protein;
    }

    private boolean containsAny(String value, String... targets) {
        for (String target : targets) {
            if (value.contains(target)) {
                return true;
            }
        }
        return false;
    }

    private void updateNewIngredientTypeButtons() {
        setTypeButtonState(addIngredientBaseButton, "Base".equals(newIngredientType));
        setTypeButtonState(addIngredientProteinButton, "Protein".equals(newIngredientType));
        setTypeButtonState(addIngredientAddonButton, "Add-On".equals(newIngredientType));
    }

    private void setTypeButtonState(Button button, boolean selected) {
        button.getStyleClass().remove("seller-picker-active-button");
        if (selected) {
            button.getStyleClass().add("seller-picker-active-button");
        }
    }

    private void clearNewIngredientForm() {
        newIngredientNameField.clear();
        newCaloriesField.setText("0");
        newProteinField.setText("0");
        newCarbsField.setText("0");
        newSugarsField.setText("0");
        newFatField.setText("0");
        newSaturatedFatField.setText("0");
        newFiberField.setText("0");
        newSodiumField.setText("0");
    }

    private void showPickerError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private SellerProductRepository.SellerProductDraft buildProductDraft(
            String productName,
            double cost,
            double price,
            int spiceLevel) {

        List<SellerProductRepository.SellerImageDraft> imageDrafts = new ArrayList<>();
        refreshImageOrders();
        for (UploadedProductImage image : productImages) {
            imageDrafts.add(new SellerProductRepository.SellerImageDraft(
                image.order,
                image.outputPath.toString()
            ));
        }

        return new SellerProductRepository.SellerProductDraft(
            productName,
            text(shortDescriptionArea.getText()),
            text(extendedDescriptionArea.getText()),
            cost,
            price,
            statusCheckBox.isSelected(),
            spiceLevel,
            selectedComboValue(countryComboBox),
            selectedChefId,
            selectedComboValue(dietaryComboBox),
            selectedComboValue(goalComboBox),
            selectedComboValue(cuisineComboBox),
            imageDrafts,
            productIngredientDrafts(),
            LocalDateTime.now().format(DATABASE_TIME)
        );
    }

    private void discardSavedForm() {
        editingProductId = -1;
        sellerTitleLabel.setText("Add New Product");
        if (saveProductButton != null) {
            saveProductButton.setText("Save Product");
        }
        productNameField.clear();
        shortDescriptionArea.clear();
        extendedDescriptionArea.clear();
        costField.clear();
        priceField.clear();
        statusCheckBox.setSelected(true);
        mainPhotoPreview.setImage(null);
        mainPhotoPlaceholder.setVisible(true);
        imageUploadStatusLabel.setText("Select multiple .png files. First image becomes main photo.");
        productImages.clear();
        imageOrderList.getChildren().clear();
        resetPickerButtons();
        clearErrors();
    }

    private String safeFilePart(String value) {
        if (value == null || value.isBlank()) {
            return "product";
        }
        return value.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private void resetPickerButtons() {
        selectedBaseIngredients.clear();
        selectedProteinIngredients.clear();
        selectedAddonIngredients.clear();
        baseSelected = false;
        proteinSelected = false;
        addonsSelected = false;
        chefSelected = false;
        selectedChefId = 1;
        pickBaseButton.setText("Pick Base");
        pickProteinButton.setText("Pick Protein");
        pickAddonsButton.setText("Pick Add-ons");
        pickChefButton.setText("Pick Chef");
    }

    private void clearErrors() {
        clearError(imageErrorLabel);
        clearError(productNameErrorLabel);
        clearError(shortDescriptionErrorLabel);
        clearError(extendedDescriptionErrorLabel);
        clearError(baseErrorLabel);
        clearError(proteinErrorLabel);
        clearError(addonErrorLabel);
        clearError(spiceErrorLabel);
        clearError(costErrorLabel);
        clearError(priceErrorLabel);
        clearError(countryErrorLabel);
        clearError(chefErrorLabel);
    }

    private void setError(Label label, String message) {
        if (label == null) {
            return;
        }
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void setWarning(Label label, String message) {
        if (label == null) {
            return;
        }
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void clearError(Label label) {
        if (label == null) {
            return;
        }
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
    }

    private int wordCount(String value) {
        String trimmed = text(value);
        if (trimmed.isBlank()) {
            return 0;
        }
        return trimmed.split("\\s+").length;
    }

    private Double parseMoney(String value) {
        String cleaned = text(value).replaceAll("[£Â]", "").trim();
        if (cleaned.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private double parseOptionalMoney(String value) {
        Double parsed = parseMoney(value);
        return parsed == null ? 0 : parsed;
    }

    private Double parseNumber(String value) {
        String cleaned = text(value);
        if (cleaned.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }

    private String selectedComboValue(ComboBox<String> comboBox) {
        if (comboBox.isEditable()) {
            String typed = text(comboBox.getEditor().getText());
            if (!typed.isBlank()) {
                return NO_COUNTRY.equalsIgnoreCase(typed) ? "" : typed;
            }
        }
        String selected = comboBox.getSelectionModel().getSelectedItem();
        if (selected != null && !selected.isBlank()) {
            return NO_COUNTRY.equalsIgnoreCase(selected) ? "" : selected;
        }
        return text(comboBox.getPromptText());
    }

    private boolean hasSelectedCountry() {
        return !selectedComboValue(countryComboBox).isBlank();
    }

    private void selectComboValue(ComboBox<String> comboBox, String value) {
        String cleanValue = text(value);
        if (cleanValue.isBlank()) {
            if (comboBox == countryComboBox) {
                cleanValue = NO_COUNTRY;
            } else {
                return;
            }
        }

        if (NO_COUNTRY.equalsIgnoreCase(cleanValue) && !comboBox.getItems().contains(NO_COUNTRY)) {
            return;
        }

        if (!comboBox.getItems().contains(cleanValue)) {
            try {
                comboBox.getItems().add(cleanValue);
            } catch (UnsupportedOperationException exception) {
                comboBox.setValue(cleanValue);
            }
        }
        if (comboBox.getItems().contains(cleanValue)) {
            comboBox.getSelectionModel().select(cleanValue);
        } else {
            comboBox.setValue(cleanValue);
        }

        if (comboBox.isEditable()) {
            comboBox.getEditor().setText(cleanValue);
            comboBox.getEditor().positionCaret(cleanValue.length());
        }
    }

    private void installSearchableCountryComboBox() {
        countryComboBox.setEditable(false);
        countryComboBox.setItems(COUNTRIES);
        countryComboBox.setVisibleRowCount(12);
    }

    private void updateCuisineFromCountry() {
        String selectedCountry = countryComboBox.getSelectionModel().getSelectedItem();
        String cuisine = cuisineForCountry(selectedCountry);
        if (cuisine != null && cuisineComboBox.getItems().contains(cuisine)) {
            cuisineComboBox.getSelectionModel().select(cuisine);
        }
    }

    private String cuisineForCountry(String country) {
        String countryCode = countryCodeForName(country);
        if (countryCode == null) {
            return null;
        }

        if (isOneOf(countryCode, "BN", "KH", "ID", "LA", "MY", "MM", "PH", "SG", "TH", "TL", "VN")) {
            return "Southeast Asian";
        }
        if (isOneOf(countryCode, "AF", "BD", "BT", "IN", "LK", "MV", "NP", "PK")) {
            return "South Asian";
        }
        if (isOneOf(countryCode, "AE", "BH", "CY", "IL", "IQ", "IR", "JO", "KW", "LB", "OM", "PS", "QA", "SA", "SY", "YE")) {
            return "Middle Eastern";
        }
        if (isOneOf(countryCode, "AD", "AL", "BA", "DZ", "EG", "ES", "FR", "GR", "HR", "IT", "LY", "MA", "MC", "ME", "MK", "MT", "PT", "RS", "SI", "SM", "TN", "TR", "VA")) {
            return "Mediterranean";
        }
        if (isOneOf(countryCode, "AM", "AZ", "CN", "GE", "HK", "JP", "KG", "KP", "KR", "KZ", "MN", "MO", "RU", "TJ", "TM", "TW", "UZ")) {
            return "Asian";
        }
        return "Western";
    }

    private String countryCodeForName(String country) {
        String cleanCountry = text(country);
        if (cleanCountry.isBlank() || NO_COUNTRY.equalsIgnoreCase(cleanCountry)) {
            return null;
        }

        for (String countryCode : Locale.getISOCountries()) {
            Locale locale = new Locale("", countryCode);
            if (cleanCountry.equals(locale.getDisplayCountry(Locale.UK))) {
                return countryCode;
            }
        }
        return null;
    }

    private boolean isOneOf(String value, String... options) {
        for (String option : options) {
            if (option.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private String shortText(String value, int maxLength) {
        String clean = text(value);
        if (clean.length() <= maxLength) {
            return clean;
        }
        return clean.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private static class UploadedProductImage {
        private int order;
        private final Path outputPath;

        private UploadedProductImage(int order, Path outputPath) {
            this.order = order;
            this.outputPath = outputPath;
        }
    }

    private static class IngredientSelection {
        private final SellerProductRepository.IngredientRecord ingredient;
        private final double extraCost;
        private final String optionGroup;
        private final String optionName;
        private final boolean selectedByDefault;

        private IngredientSelection(SellerProductRepository.IngredientRecord ingredient, double extraCost) {
            this(ingredient, extraCost, null, ingredient.name, true);
        }

        private IngredientSelection(
                SellerProductRepository.IngredientRecord ingredient,
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
}
