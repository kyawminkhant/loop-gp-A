package loop.reviews.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import loop.reviews.SceneManager;
import loop.reviews.Session;
import loop.reviews.db.OrderDao;
import loop.reviews.db.ProductDao;
import loop.reviews.db.ReviewDao;
import loop.reviews.model.Product;
import loop.reviews.model.Review;
import loop.reviews.util.Toast;
import loop.reviews.util.Validation;

import java.io.File;

/** FR3 (submit review) + FR10 (duplicate & character validation) + FR2 (purchase check). */
public class SubmitReviewController {

    @FXML private StackPane root;
    @FXML private Label productNameLabel;
    @FXML private Label eligibilityLabel;
    @FXML private ComboBox<Integer> ratingCombo;
    @FXML private Label ratingPreview;
    @FXML private TextArea commentArea;
    @FXML private TextField imageField;
    @FXML private Label errorLabel;
    @FXML private Button submitButton;

    private final ProductDao productDao = new ProductDao();
    private final ReviewDao reviewDao = new ReviewDao();
    private final OrderDao orderDao = new OrderDao();

    private Product product;

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
        submitButton.setDisable(true);
    }

    @FXML
    private void browseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select an image");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File f = chooser.showOpenDialog(SceneManager.getStage());
        if (f != null) imageField.setText(f.getAbsolutePath());
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
        r.setImageUrl(Validation.isBlank(imageField.getText()) ? null : imageField.getText());
        r.setCreatedAt(System.currentTimeMillis());
        r.setStatus(Review.ACTIVE);
        r.setEditDurationSeconds(300);
        try {
            reviewDao.insert(r);
        } catch (RuntimeException ex) {
            // Unique constraint safety net.
            errorLabel.setText("You already reviewed this.");
            return;
        }
        productDao.recalculateAverage(product.getId());   // FR9
        Toast.show(root, "Review submitted — thank you!", false);
        SceneManager.switchTo("product_reviews");
    }

    @FXML private void goBack() { SceneManager.switchTo("product_reviews"); }
}
