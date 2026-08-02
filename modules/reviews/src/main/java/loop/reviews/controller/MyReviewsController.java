package loop.reviews.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import loop.reviews.SceneManager;
import loop.reviews.Session;
import loop.reviews.db.ProductDao;
import loop.reviews.db.ReviewDao;
import loop.reviews.model.Product;
import loop.reviews.model.Review;
import loop.reviews.util.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** FR4 (edit) + FR5 (delete) within the configurable time window, with live countdown. */
public class MyReviewsController {

    @FXML private StackPane root;
    @FXML private VBox reviewList;

    private final ReviewDao reviewDao = new ReviewDao();
    private final ProductDao productDao = new ProductDao();

    // Track countdown widgets so we can update them each second without rebuilding.
    private final List<Row> rows = new ArrayList<>();
    private Timeline ticker;

    private static class Row {
        Review review;
        Label countdown;
        Button edit;
        Button delete;
    }

    @FXML
    private void initialize() {
        if (Session.getCurrentUser() == null) { SceneManager.switchTo("home"); return; }
        buildList();
        ticker = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        ticker.setCycleCount(Animation.INDEFINITE);
        ticker.play();
    }

    private void buildList() {
        stopTicker();
        rows.clear();
        reviewList.getChildren().clear();

        List<Review> mine = reviewDao.findByCustomer(Session.getCurrentUser().getId());
        if (mine.isEmpty()) {
            Label empty = new Label("You haven't written any reviews yet.");
            empty.getStyleClass().add("empty-state");
            reviewList.getChildren().add(empty);
            return;
        }
        for (Review r : mine) {
            reviewList.getChildren().add(buildCard(r));
        }
    }

    private VBox buildCard(Review r) {
        Product p = productDao.findById(r.getProductId());

        VBox card = new VBox(8);
        card.getStyleClass().add("review-card");

        HBox head = new HBox(10);
        head.setAlignment(Pos.CENTER_LEFT);
        Label name = new Label(p == null ? "Product" : p.getName());
        name.getStyleClass().add("review-author");
        Label stars = new Label(HomeController.starString(r.getRating()));
        stars.getStyleClass().add("stars");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label status = new Label(r.getStatus());
        status.getStyleClass().add("status-" + r.getStatus().toLowerCase());
        head.getChildren().addAll(name, stars, sp, status);

        Label comment = new Label(r.getCommentText());
        comment.getStyleClass().add("review-comment");
        comment.setWrapText(true);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        Label countdown = new Label();
        countdown.getStyleClass().add("countdown");
        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);
        Button edit = new Button("Edit");
        edit.getStyleClass().add("btn-ghost");
        Button delete = new Button("Delete");
        delete.getStyleClass().add("btn-danger");
        edit.setOnAction(e -> onEdit(r));
        delete.setOnAction(e -> onDelete(r));
        actions.getChildren().addAll(countdown, sp2, edit, delete);

        card.getChildren().addAll(head, comment, actions);

        Row row = new Row();
        row.review = r; row.countdown = countdown; row.edit = edit; row.delete = delete;
        rows.add(row);
        updateRow(row);
        return card;
    }

    private void tick() {
        for (Row row : rows) updateRow(row);
    }

    private void updateRow(Row row) {
        long remaining = row.review.remainingSeconds();
        boolean open = remaining > 0 && !Review.REMOVED.equals(row.review.getStatus());
        if (open) {
            long m = remaining / 60, s = remaining % 60;
            row.countdown.setText(String.format("Edit window: %d:%02d left", m, s));
            row.countdown.getStyleClass().setAll("countdown");
        } else {
            row.countdown.setText("Edit window closed — locked");
            row.countdown.getStyleClass().setAll("countdown-closed");
        }
        row.edit.setDisable(!open);
        row.delete.setDisable(!open);
    }

    private void onEdit(Review r) {
        if (!r.isEditWindowOpen()) {
            Toast.show(root, "The edit window for this review has closed.", true);
            return;
        }
        Session.setEditingReviewId(r.getId());
        stopTicker();
        SceneManager.switchTo("edit_review");
    }

    private void onDelete(Review r) {
        if (!r.isEditWindowOpen()) {
            Toast.show(root, "The delete window for this review has closed.", true);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete this review permanently?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.YES) {
            int productId = r.getProductId();
            reviewDao.delete(r.getId());
            productDao.recalculateAverage(productId);   // FR5 -> FR9
            Toast.show(root, "Review deleted. Average rating updated.", false);
            buildList();
            ticker = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
            ticker.setCycleCount(Animation.INDEFINITE);
            ticker.play();
        }
    }

    private void stopTicker() {
        if (ticker != null) { ticker.stop(); ticker = null; }
    }

    @FXML private void goHome() {
        stopTicker();
        SceneManager.switchTo("home");
    }
}
