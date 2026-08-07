package loop.reviews.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import loop.reviews.SceneManager;
import loop.reviews.Session;
import loop.reviews.db.ProductDao;
import loop.reviews.db.ReviewDao;
import loop.reviews.model.Product;
import loop.reviews.model.Review;
import loop.reviews.util.ContentModeration;
import loop.reviews.util.Toast;
import loop.reviews.util.Validation;

/** FR4 - edit a review while the configurable window remains open (live countdown). */
public class EditReviewController {

    @FXML private StackPane root;
    @FXML private Label productNameLabel;
    @FXML private Label countdownLabel;
    @FXML private ComboBox<Integer> ratingCombo;
    @FXML private Label ratingPreview;
    @FXML private TextArea commentArea;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;

    private final ReviewDao reviewDao = new ReviewDao();
    private final ProductDao productDao = new ProductDao();

    private Review review;
    private Timeline ticker;

    @FXML
    private void initialize() {
        review = reviewDao.findById(Session.getEditingReviewId());
        if (review == null) { SceneManager.switchTo("my_reviews"); return; }

        Product p = productDao.findById(review.getProductId());
        productNameLabel.setText(p == null ? "Product" : p.getName());

        ratingCombo.getItems().setAll(1, 2, 3, 4, 5);
        ratingCombo.valueProperty().addListener((o, old, val) ->
            ratingPreview.setText(val == null ? "" : HomeController.starString(val)));
        ratingCombo.getSelectionModel().select(Integer.valueOf(review.getRating()));
        commentArea.setText(review.getCommentText());

        ticker = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateCountdown()));
        ticker.setCycleCount(Animation.INDEFINITE);
        ticker.play();
        updateCountdown();
    }

    private void updateCountdown() {
        long remaining = review.remainingSeconds();
        if (remaining > 0) {
            long m = remaining / 60, s = remaining % 60;
            countdownLabel.setText(String.format("%d:%02d left to edit", m, s));
            countdownLabel.getStyleClass().setAll("countdown");
        } else {
            countdownLabel.setText("Edit window closed");
            countdownLabel.getStyleClass().setAll("countdown-closed");
            // Lock the form once the window elapses mid-edit.
            ratingCombo.setDisable(true);
            commentArea.setDisable(true);
            saveButton.setDisable(true);
            if (ticker != null) ticker.stop();
        }
    }

    @FXML
    private void handleSave() {
        errorLabel.setText("");
        if (!review.isEditWindowOpen()) {           // FR4 expiry guard
            errorLabel.setText("The edit window has closed; changes cannot be saved.");
            return;
        }
        Integer rating = ratingCombo.getValue();
        String comment = commentArea.getText();
        if (rating == null || !Validation.isValidRating(rating)) {
            errorLabel.setText("Rating must be between 1 and 5.");
            return;
        }
        if (Validation.isBlank(comment)) {
            errorLabel.setText("Comment cannot be empty.");
            return;
        }
        String bad = Validation.firstDisallowedChar(comment);
        if (bad != null) {
            errorLabel.setText("Comment contains a disallowed character: " + bad);
            return;
        }
        String moderationReason = ContentModeration.flagReason(comment);
        String status = review.getStatus();
        if (Review.ACTIVE.equals(status) && moderationReason != null) {
            status = Review.FLAGGED;
        }
        reviewDao.updateCustomerContent(review.getId(), rating, comment.trim(), status);
        productDao.recalculateAverage(review.getProductId());   // FR9
        if (ticker != null) ticker.stop();
        Toast.show(root, moderationReason == null
                ? "Review updated."
                : "Review updated and sent for an administrator to check.", false);
        SceneManager.switchTo("my_reviews");
    }

    @FXML
    private void goBack() {
        if (ticker != null) ticker.stop();
        SceneManager.switchTo("my_reviews");
    }
}
