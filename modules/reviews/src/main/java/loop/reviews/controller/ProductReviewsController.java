package loop.reviews.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import loop.reviews.SceneManager;
import loop.reviews.Session;
import loop.reviews.db.HelpfulVoteDao;
import loop.reviews.db.ProductDao;
import loop.reviews.db.ReviewDao;
import loop.reviews.model.HelpfulVote;
import loop.reviews.model.Product;
import loop.reviews.model.Review;
import loop.reviews.util.ContentModeration;
import loop.reviews.util.ReviewImageService;
import loop.reviews.util.ReviewPhotoView;
import loop.reviews.util.Toast;
import loop.reviews.util.Validation;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** FR6 (view/sort/filter), FR7 (helpful voting), FR9 (average + distribution). */
public class ProductReviewsController {

    @FXML private StackPane root;
    @FXML private Label productNameLabel;
    @FXML private Label productMetaLabel;
    @FXML private Label avgStarsLabel;
    @FXML private Label avgValueLabel;
    @FXML private Label reviewCountLabel;
    @FXML private VBox distributionBox;
    @FXML private ComboBox<String> sortCombo;
    @FXML private ComboBox<String> starFilterCombo;
    @FXML private TextField keywordField;
    @FXML private VBox reviewList;
    @FXML private Button writeButton;
    @FXML private Button backButton;

    private final ProductDao productDao = new ProductDao();
    private final ReviewDao reviewDao = new ReviewDao();
    private final HelpfulVoteDao voteDao = new HelpfulVoteDao();

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm").withZone(ZoneId.systemDefault());

    private Product product;

    @FXML
    private void initialize() {
        product = productDao.findById(Session.getSelectedProductId());
        if (product == null) {
            SceneManager.switchTo("home");
            return;
        }
        // Admins moderate elsewhere; they don't write reviews.
        writeButton.setVisible(!Session.isAdmin());
        writeButton.setManaged(!Session.isAdmin());
        backButton.setText(Session.hasProductReturnNavigator() ? "< Back to Food" : "< Back");

        sortCombo.getItems().setAll(
                "Most Helpful", "Newest", "Highest Rated", "Lowest Rated");
        sortCombo.getSelectionModel().selectFirst();
        starFilterCombo.getItems().setAll("All stars", "5", "4", "3", "2", "1");
        starFilterCombo.getSelectionModel().selectFirst();

        renderHeader();
        renderDistribution();
        renderReviews();
    }

    private void renderHeader() {
        productNameLabel.setText(product.getName());
        productMetaLabel.setText(String.format("%s  ·  £%.2f",
                product.getCategory() == null ? "" : product.getCategory(), product.getPrice()));
        double avg = product.getAverageRating();
        avgStarsLabel.setText(HomeController.starString(avg));
        avgValueLabel.setText(String.format("%.1f / 5", avg));
    }

    private void renderDistribution() {
        // Remove any previously-rendered rows (keep the title at index 0).
        distributionBox.getChildren().remove(1, distributionBox.getChildren().size());
        int[] dist = reviewDao.ratingDistribution(product.getId());
        int total = 0;
        for (int s = 1; s <= 5; s++) total += dist[s];
        reviewCountLabel.setText(total + (total == 1 ? " review" : " reviews"));

        for (int s = 5; s >= 1; s--) {
            int count = dist[s];
            double frac = total == 0 ? 0 : (double) count / total;

            Label starLbl = new Label(s + " ★");
            starLbl.getStyleClass().add("dist-star");
            starLbl.setMinWidth(38);

            Region fill = new Region();
            fill.getStyleClass().add("dist-fill");
            fill.setMinWidth(Math.max(2, 360 * frac));
            fill.setPrefWidth(Math.max(2, 360 * frac));

            Region track = new Region();
            track.getStyleClass().add("dist-track");
            HBox.setHgrow(track, Priority.ALWAYS);

            Label countLbl = new Label(String.valueOf(count));
            countLbl.getStyleClass().add("dist-count");
            countLbl.setMinWidth(28);

            HBox row = new HBox(10, starLbl, fill, track, countLbl);
            row.setAlignment(Pos.CENTER_LEFT);
            distributionBox.getChildren().add(row);
        }
    }

    private void renderReviews() {
        reviewList.getChildren().clear();

        String sort;
        String selectedSort = sortCombo.getValue();
        if ("Newest".equals(selectedSort)) {
            sort = "date";
        } else if ("Highest Rated".equals(selectedSort)) {
            sort = "rating_desc";
        } else if ("Lowest Rated".equals(selectedSort)) {
            sort = "rating_asc";
        } else {
            sort = "helpful";
        }
        int minStars = 0;
        String starSel = starFilterCombo.getSelectionModel().getSelectedItem();
        if (starSel != null && !starSel.startsWith("All")) minStars = Integer.parseInt(starSel);
        String keyword = keywordField.getText();

        List<Review> reviews = reviewDao.findByProduct(product.getId(), sort, minStars, keyword);
        if (Session.getCurrentUser() != null && !Session.isAdmin()) {
            int currentCustomerId = Session.getCurrentUser().getId();
            reviews.sort(java.util.Comparator.comparing(
                    (Review review) -> review.getCustomerId() != currentCustomerId));
        }
        if (reviews.isEmpty()) {
            Label empty = new Label("No reviews yet. Be the first to share your thoughts!");
            empty.getStyleClass().add("empty-state");
            reviewList.getChildren().add(empty);
            return;
        }
        Map<Integer, String> customerVotes = Session.getCurrentUser() != null && !Session.isAdmin()
                ? voteDao.findVoteTypesByCustomer(Session.getCurrentUser().getId())
                : Map.of();
        for (Review r : reviews) {
            reviewList.getChildren().add(buildReviewCard(r, customerVotes.get(r.getId())));
        }
    }

    private VBox buildReviewCard(Review r, String selectedVote) {
        VBox card = new VBox(8);
        card.getStyleClass().add("review-card");

        HBox head = new HBox(10);
        head.setAlignment(Pos.CENTER_LEFT);
        Label who = new Label(r.getCustomerName() == null ? "Customer" : r.getCustomerName());
        who.getStyleClass().add("review-author");
        boolean ownReview = Session.getCurrentUser() != null
                && !Session.isAdmin()
                && Session.getCurrentUser().getId() == r.getCustomerId();
        Label ownBadge = new Label("Your review");
        ownBadge.getStyleClass().add("badge-own-review");
        ownBadge.setVisible(ownReview);
        ownBadge.setManaged(ownReview);
        Label stars = new Label(HomeController.starString(r.getRating()));
        stars.getStyleClass().add("stars");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label date = new Label(FMT.format(Instant.ofEpochMilli(r.getCreatedAt())));
        date.getStyleClass().add("review-date");
        head.getChildren().addAll(who, ownBadge, stars, sp, date);

        Label comment = new Label(r.getCommentText());
        comment.getStyleClass().add("review-comment");
        comment.setWrapText(true);

        StackPane photo = ReviewPhotoView.create(r.getImageUrl());

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        Button up = new Button("👍 Helpful (" + r.getHelpfulCount() + ")");
        up.getStyleClass().add("btn-vote");
        Button down = new Button("👎 Unhelpful (" + r.getUnhelpfulCount() + ")");
        down.getStyleClass().add("btn-vote");
        up.setOnAction(e -> vote(r, HelpfulVote.HELPFUL));
        down.setOnAction(e -> vote(r, HelpfulVote.UNHELPFUL));
        if (HelpfulVote.HELPFUL.equals(selectedVote)) {
            up.getStyleClass().add("btn-vote-selected");
        } else if (HelpfulVote.UNHELPFUL.equals(selectedVote)) {
            down.getStyleClass().add("btn-vote-selected");
        }

        // Only authenticated customers vote (FR7); admins view only.
        boolean canVote = Session.getCurrentUser() != null && !Session.isAdmin();
        up.setDisable(!canVote);
        down.setDisable(!canVote);
        actions.getChildren().addAll(up, down);
        if (ownReview) {
            Region actionSpacer = new Region();
            HBox.setHgrow(actionSpacer, Priority.ALWAYS);
            Button edit = new Button(r.isEditWindowOpen()
                    ? "Edit your review" : "Edit window closed");
            edit.getStyleClass().add("btn-ghost");
            edit.setDisable(!r.isEditWindowOpen());
            edit.setOnAction(event -> beginInlineEdit(card, r));
            Button delete = new Button("Delete your review");
            delete.getStyleClass().add("btn-danger");
            delete.setDisable(!r.isEditWindowOpen());
            delete.setOnAction(event -> deleteOwnReview(r));
            actions.getChildren().addAll(actionSpacer, edit, delete);
            card.getStyleClass().add("review-card-own");
        }
        card.getChildren().addAll(head, comment);
        if (photo != null) {
            card.getChildren().add(photo);
        }
        card.getChildren().add(actions);
        return card;
    }

    private void beginInlineEdit(VBox card, Review review) {
        if (!isOwnedByCurrentCustomer(review) || !review.isEditWindowOpen()) {
            Toast.show(root, "The edit window for this review has closed.", true);
            renderReviews();
            return;
        }

        Label heading = new Label("Edit your review");
        heading.getStyleClass().add("section-title");

        ComboBox<Integer> rating = new ComboBox<>();
        rating.getItems().setAll(1, 2, 3, 4, 5);
        rating.getSelectionModel().select(Integer.valueOf(review.getRating()));
        Label starPreview = new Label(HomeController.starString(review.getRating()));
        starPreview.getStyleClass().add("stars");
        rating.valueProperty().addListener((observable, oldValue, newValue) ->
                starPreview.setText(newValue == null
                        ? "" : HomeController.starString(newValue)));
        HBox ratingRow = new HBox(10, new Label("Rating"), rating, starPreview);
        ratingRow.setAlignment(Pos.CENTER_LEFT);

        TextArea comment = new TextArea(review.getCommentText());
        comment.setWrapText(true);
        comment.setPrefRowCount(4);
        Label limit = new Label("Maximum " + Validation.MAX_COMMENT_LENGTH + " characters.");
        limit.getStyleClass().add("hint");
        Label error = new Label();
        error.getStyleClass().add("error-text");
        error.setWrapText(true);

        Button save = new Button("Save changes");
        save.getStyleClass().add("btn-primary");
        Button cancel = new Button("Cancel");
        cancel.getStyleClass().add("btn-ghost");
        save.setOnAction(event -> saveInlineEdit(review, rating, comment, error));
        cancel.setOnAction(event -> renderReviews());

        Label countdown = new Label(formatEditTime(review.remainingSeconds()));
        countdown.getStyleClass().add("countdown");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actions = new HBox(10, countdown, spacer, save, cancel);
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().setAll(heading, ratingRow, comment, limit, error, actions);
    }

    private void saveInlineEdit(
            Review displayedReview,
            ComboBox<Integer> rating,
            TextArea commentArea,
            Label error) {
        Review current = reviewDao.findById(displayedReview.getId());
        if (current == null || !isOwnedByCurrentCustomer(current)) {
            error.setText("This review is no longer available.");
            return;
        }
        if (!current.isEditWindowOpen()) {
            error.setText("The edit window has closed; changes cannot be saved.");
            return;
        }

        Integer selectedRating = rating.getValue();
        String comment = commentArea.getText();
        if (selectedRating == null || !Validation.isValidRating(selectedRating)) {
            error.setText("Rating must be between 1 and 5.");
            return;
        }
        if (Validation.isBlank(comment)) {
            error.setText("Comment cannot be empty.");
            return;
        }
        if (Validation.exceedsCommentLimit(comment)) {
            error.setText("Review must be at most "
                    + Validation.MAX_COMMENT_LENGTH + " characters.");
            return;
        }
        String bad = Validation.firstDisallowedChar(comment);
        if (bad != null) {
            error.setText("Comment contains a disallowed character: " + bad);
            return;
        }

        String moderationReason = ContentModeration.flagReason(comment);
        String status = current.getStatus();
        if (Review.ACTIVE.equals(status) && moderationReason != null) {
            status = Review.FLAGGED;
        }
        reviewDao.updateCustomerContent(
                current.getId(), selectedRating, comment.trim(), status);
        productDao.recalculateAverage(current.getProductId());
        product = productDao.findById(current.getProductId());
        renderHeader();
        renderDistribution();
        renderReviews();
        Toast.show(root, moderationReason == null
                ? "Review updated."
                : "Review updated and sent for an administrator to check.", false);
    }

    private boolean isOwnedByCurrentCustomer(Review review) {
        return Session.getCurrentUser() != null
                && !Session.isAdmin()
                && Session.getCurrentUser().getId() == review.getCustomerId();
    }

    private void deleteOwnReview(Review displayedReview) {
        Review current = reviewDao.findById(displayedReview.getId());
        if (current == null || !isOwnedByCurrentCustomer(current)) {
            Toast.show(root, "This review is no longer available.", true);
            renderReviews();
            return;
        }
        if (!current.isEditWindowOpen()) {
            Toast.show(root, "The delete window for this review has closed.", true);
            renderReviews();
            return;
        }

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Delete your review permanently?",
                ButtonType.YES,
                ButtonType.NO);
        confirmation.setTitle("Delete Review");
        confirmation.setHeaderText("This action cannot be undone.");
        Optional<ButtonType> choice = confirmation.showAndWait();
        if (choice.isEmpty() || choice.get() != ButtonType.YES) {
            return;
        }

        int productId = current.getProductId();
        reviewDao.delete(current.getId());
        ReviewImageService.deleteManagedImage(current.getImageUrl());
        productDao.recalculateAverage(productId);
        product = productDao.findById(productId);
        renderHeader();
        renderDistribution();
        renderReviews();
        Toast.show(root, "Your review was deleted and the rating was updated.", false);
    }

    private String formatEditTime(long remainingSeconds) {
        long minutes = remainingSeconds / 60;
        long seconds = remainingSeconds % 60;
        return String.format("Edit window: %d:%02d left", minutes, seconds);
    }

    private void vote(Review r, String voteType) {
        int customerId = Session.getCurrentUser().getId();
        HelpfulVoteDao.ToggleResult result = voteDao.toggle(r.getId(), customerId, voteType);
        String choice = HelpfulVote.HELPFUL.equals(voteType) ? "Helpful" : "Unhelpful";
        if (result == HelpfulVoteDao.ToggleResult.REMOVED) {
            Toast.show(root, "Your vote was removed.", false);
        } else if (result == HelpfulVoteDao.ToggleResult.SWITCHED) {
            Toast.show(root, "Your vote was changed to " + choice + ".", false);
        } else {
            Toast.show(root, "Marked as " + choice + ".", false);
        }
        renderReviews();
    }

    @FXML private void applyFilters() { renderReviews(); }

    @FXML private void clearFilters() {
        sortCombo.getSelectionModel().selectFirst();
        starFilterCombo.getSelectionModel().selectFirst();
        keywordField.clear();
        renderReviews();
    }

    @FXML private void openSubmit() {
        SceneManager.switchTo("submit_review");
    }

    @FXML private void openAnalytics() {
        SceneManager.switchTo("analytics");
    }

    @FXML private void goBack() {
        if (!Session.returnToProduct()) {
            SceneManager.switchTo("home");
        }
    }
}
