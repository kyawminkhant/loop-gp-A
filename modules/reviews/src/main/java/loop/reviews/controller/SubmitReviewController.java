package loop.reviews.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import loop.reviews.SceneManager;
import loop.reviews.Session;
import loop.reviews.db.OrderDao;
import loop.reviews.db.ProductDao;
import loop.reviews.db.ReviewDao;
import loop.reviews.model.Product;
import loop.reviews.model.Review;
import loop.reviews.util.ContentModeration;
import loop.reviews.util.ReviewImageService;
import loop.reviews.util.Toast;
import loop.reviews.util.Validation;

import java.io.File;
import java.io.IOException;

/** FR3 (submit review) + FR10 (duplicate & character validation) + FR2 (purchase check). */
public class SubmitReviewController {

    @FXML private StackPane root;
    @FXML private Label productNameLabel;
    @FXML private Label eligibilityLabel;
    @FXML private ComboBox<Integer> ratingCombo;
    @FXML private Label ratingPreview;
    @FXML private TextArea commentArea;
    @FXML private TextField imageField;
    @FXML private StackPane imagePreviewBox;
    @FXML private ImageView imagePreview;
    @FXML private Label errorLabel;
    @FXML private Button submitButton;
    @FXML private Button browseImageButton;
    @FXML private Button removeImageButton;

    private final ProductDao productDao = new ProductDao();
    private final ReviewDao reviewDao = new ReviewDao();
    private final OrderDao orderDao = new OrderDao();

    private Product product;
    private File selectedImageFile;

    @FXML
    private void initialize() {
        product = productDao.findById(Session.getSelectedProductId());
        if (product == null || Session.getCurrentUser() == null) {
            SceneManager.switchTo("home");
            return;
        }
        productNameLabel.setText(product.getName());

        ratingCombo.getItems().setAll(1, 2, 3, 4, 5);
        ratingCombo.valueProperty().addListener((obs, old, val) ->
            ratingPreview.setText(val == null ? "" : HomeController.starString(val)));
        ratingCombo.getSelectionModel().select(Integer.valueOf(5));

        int customerId = Session.getCurrentUser().getId();
        boolean purchased = orderDao.verifyPurchase(customerId, product.getId());
        boolean duplicate = reviewDao.existsForCustomerAndProduct(customerId, product.getId());

        if (!purchased) {
            eligibilityLabel.setText("You can only review products you have purchased. "
                    + "This product is not in your order history.");
            eligibilityLabel.getStyleClass().setAll("error-text");
            disableForm();
        } else if (duplicate) {
            eligibilityLabel.setText("You already reviewed this product. "
                    + "You can edit it from 'My reviews_reviews' while the edit window is open.");
            eligibilityLabel.getStyleClass().setAll("error-text");
            disableForm();
        } else {
            eligibilityLabel.setText("Verified purchase — you're all set to leave a review.");
        }
    }

    private void disableForm() {
        ratingCombo.setDisable(true);
        commentArea.setDisable(true);
        imageField.setDisable(true);
        browseImageButton.setDisable(true);
        removeImageButton.setDisable(true);
        submitButton.setDisable(true);
    }

    @FXML
    private void browseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select an image");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File f = chooser.showOpenDialog(SceneManager.getStage());
        if (f == null) {
            return;
        }
        try {
            ReviewImageService.validate(f);
            selectedImageFile = f;
            imageField.setText(f.getName());
            imagePreview.setImage(new Image(
                    f.toURI().toString(), 520, 230, true, true, false));
            imagePreviewBox.setVisible(true);
            imagePreviewBox.setManaged(true);
            removeImageButton.setVisible(true);
            removeImageButton.setManaged(true);
            errorLabel.setText("");
        } catch (IOException exception) {
            clearSelectedImage();
            errorLabel.setText(exception.getMessage());
        }
    }

    @FXML
    private void clearSelectedImage() {
        selectedImageFile = null;
        imageField.clear();
        imagePreview.setImage(null);
        imagePreviewBox.setVisible(false);
        imagePreviewBox.setManaged(false);
        removeImageButton.setVisible(false);
        removeImageButton.setManaged(false);
    }

    @FXML
    private void handleSubmit() {
        errorLabel.setText("");
        Integer rating = ratingCombo.getValue();
        String comment = commentArea.getText();

        if (rating == null || !Validation.isValidRating(rating)) {
            errorLabel.setText("Please choose a star rating between 1 and 5.");
            return;
        }
        if (Validation.isBlank(comment)) {
            errorLabel.setText("Please write a comment before submitting.");
            return;
        }
        String bad = Validation.firstDisallowedChar(comment);
        if (bad != null) {
            errorLabel.setText("Comment contains a disallowed character: " + bad);
            return;
        }

        int customerId = Session.getCurrentUser().getId();
        // Re-check server-side rules (defence in depth).
        if (!orderDao.verifyPurchase(customerId, product.getId())) {
            errorLabel.setText("Purchase could not be verified for this product.");
            return;
        }
        if (reviewDao.existsForCustomerAndProduct(customerId, product.getId())) {
            errorLabel.setText("You already reviewed this."); // FR10 duplicate message
            return;
        }

        Review r = new Review();
        r.setProductId(product.getId());
        r.setCustomerId(customerId);
        r.setRating(rating);
        r.setCommentText(comment.trim());
        r.setCreatedAt(System.currentTimeMillis());
        String moderationReason = ContentModeration.flagReason(comment);
        r.setStatus(moderationReason == null ? Review.ACTIVE : Review.FLAGGED);
        r.setEditDurationSeconds(300);
        String storedImagePath = null;
        try {
            if (selectedImageFile != null) {
                storedImagePath = ReviewImageService.importImage(
                        selectedImageFile, customerId, product.getId());
            }
            r.setImageUrl(storedImagePath);
            reviewDao.insert(r);
        } catch (IOException exception) {
            errorLabel.setText(exception.getMessage());
            return;
        } catch (RuntimeException ex) {
            ReviewImageService.deleteManagedImage(storedImagePath);
            errorLabel.setText(reviewDao.existsForCustomerAndProduct(customerId, product.getId())
                    ? "You already reviewed this."
                    : "The review could not be saved. Please try again.");
            return;
        }
        productDao.recalculateAverage(product.getId());   // FR9
        Toast.show(root, moderationReason == null
                ? "Review submitted — thank you!"
                : "Review submitted for an administrator to check.", false);
        SceneManager.switchTo("product_reviews");
    }

    @FXML private void goBack() { SceneManager.switchTo("product_reviews"); }
}
