package loop.reviews.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
import loop.reviews.util.Toast;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

        sortCombo.getItems().setAll("Most Helpful", "Newest", "Highest Rated");
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

        String sort = "helpful";
        switch (sortCombo.getSelectionModel().getSelectedIndex()) {
            case 1: sort = "date"; break;
            case 2: sort = "rating"; break;
            default: sort = "helpful";
        }
        int minStars = 0;
        String starSel = starFilterCombo.getSelectionModel().getSelectedItem();
        if (starSel != null && !starSel.startsWith("All")) minStars = Integer.parseInt(starSel);
        String keyword = keywordField.getText();

        List<Review> reviews = reviewDao.findByProduct(product.getId(), sort, minStars, keyword);
        if (reviews.isEmpty()) {
            Label empty = new Label("No reviews yet. Be the first to share your thoughts!");
            empty.getStyleClass().add("empty-state");
            reviewList.getChildren().add(empty);
            return;
        }
        for (Review r : reviews) {
            reviewList.getChildren().add(buildReviewCard(r));
        }
    }

    private VBox buildReviewCard(Review r) {
        VBox card = new VBox(8);
        card.getStyleClass().add("review-card");

        HBox head = new HBox(10);
        head.setAlignment(Pos.CENTER_LEFT);
        Label who = new Label(r.getCustomerName() == null ? "Customer" : r.getCustomerName());
        who.getStyleClass().add("review-author");
        Label stars = new Label(HomeController.starString(r.getRating()));
        stars.getStyleClass().add("stars");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label date = new Label(FMT.format(Instant.ofEpochMilli(r.getCreatedAt())));
        date.getStyleClass().add("review-date");
        head.getChildren().addAll(who, stars, sp, date);

        Label comment = new Label(r.getCommentText());
        comment.getStyleClass().add("review-comment");
        comment.setWrapText(true);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        Button up = new Button("👍 Helpful (" + r.getHelpfulCount() + ")");
        up.getStyleClass().add("btn-vote");
        Button down = new Button("👎 Unhelpful (" + r.getUnhelpfulCount() + ")");
        down.getStyleClass().add("btn-vote");
        up.setOnAction(e -> vote(r, HelpfulVote.HELPFUL));
        down.setOnAction(e -> vote(r, HelpfulVote.UNHELPFUL));

        // Only authenticated customers vote (FR7); admins view only.
        boolean canVote = !Session.isAdmin();
        up.setDisable(!canVote);
        down.setDisable(!canVote);

        actions.getChildren().addAll(up, down);
        card.getChildren().addAll(head, comment, actions);
        return card;
    }

    private void vote(Review r, String voteType) {
        int customerId = Session.getCurrentUser().getId();
        if (voteDao.hasVoted(r.getId(), customerId)) {          // FR7 duplicate prevention
            Toast.show(root, "You have already voted on this review.", true);
            return;
        }
        voteDao.insert(new HelpfulVote(r.getId(), customerId, voteType));
        reviewDao.adjustHelpful(r.getId(), voteType);
        Toast.show(root, "Thanks for your feedback!", false);
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
        SceneManager.switchTo("home");
    }
}
