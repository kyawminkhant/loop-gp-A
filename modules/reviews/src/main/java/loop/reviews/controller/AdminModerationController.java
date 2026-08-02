package loop.reviews.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import loop.reviews.SceneManager;
import loop.reviews.Session;
import loop.reviews.db.ModerationLogDao;
import loop.reviews.db.ProductDao;
import loop.reviews.db.ReviewDao;
import loop.reviews.model.ModerationLog;
import loop.reviews.model.Product;
import loop.reviews.model.Review;
import loop.reviews.util.Toast;
import loop.reviews.util.Validation;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * FR8 - admin moderation. The screen is product-first: the admin sees a list of
 * products and opens one to moderate just that product's reviews (flag, edit,
 * remove/delete, restore). This keeps moderation manageable when there are many
 * reviews. Every action is written to the audit log.
 */
public class AdminModerationController {

    @FXML private StackPane root;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField searchField;
    @FXML private VBox reviewList;
    @FXML private VBox logList;

    private final ReviewDao reviewDao = new ReviewDao();
    private final ProductDao productDao = new ProductDao();
    private final ModerationLogDao logDao = new ModerationLogDao();

    /** null = showing the product list; otherwise showing one product's reviews. */
    private Integer selectedProductId = null;

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("d MMM, HH:mm").withZone(ZoneId.systemDefault());

    @FXML
    private void initialize() {
        if (!Session.isAdmin()) { SceneManager.switchTo("home"); return; }
        statusFilter.getItems().setAll("All", "Active", "Flagged", "Removed");
        statusFilter.getSelectionModel().selectFirst();
        statusFilter.valueProperty().addListener((obs, oldV, newV) -> render());
        render();
        renderLog();
    }

    /** Decide which view to show. */
    private void render() {
        if (selectedProductId == null) {
            renderProducts();
        } else {
            renderReviewsForProduct(selectedProductId);
        }
    }

    // ---------- Product list view ----------

    private void renderProducts() {
        reviewList.getChildren().clear();

        Label heading = new Label("Select a product to moderate its reviews_reviews");
        heading.getStyleClass().add("page-subtitle");
        reviewList.getChildren().add(heading);

        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        List<Review> allReviews = reviewDao.findAll();

        int shown = 0;
        for (Product p : productDao.findAll()) {
            if (!q.isEmpty() && !p.getName().toLowerCase().contains(q)) continue;

            int total = 0, flagged = 0, removed = 0;
            for (Review r : allReviews) {
                if (r.getProductId() != p.getId()) continue;
                total++;
                if (Review.FLAGGED.equals(r.getStatus())) flagged++;
                if (Review.REMOVED.equals(r.getStatus())) removed++;
            }
            reviewList.getChildren().add(buildProductCard(p, total, flagged, removed));
            shown++;
        }
        if (shown == 0) {
            Label empty = new Label("No products match your search.");
            empty.getStyleClass().add("empty-state");
            reviewList.getChildren().add(empty);
        }
    }

    private HBox buildProductCard(Product p, int total, int flagged, int removed) {
        HBox card = new HBox(14);
        card.getStyleClass().add("product-card");
        card.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(4);
        Label name = new Label(p.getName());
        name.getStyleClass().add("product-name");
        String meta = total + (total == 1 ? " review" : " reviews");
        if (flagged > 0) meta += "  ·  " + flagged + " flagged";
        if (removed > 0) meta += "  ·  " + removed + " removed";
        Label metaLabel = new Label(meta);
        metaLabel.getStyleClass().add("product-meta");
        info.getChildren().addAll(name, metaLabel);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Button manage = new Button("Manage reviews");
        manage.getStyleClass().add("btn-primary");
        manage.setDisable(total == 0);
        manage.setOnAction(e -> {
            selectedProductId = p.getId();
            searchField.clear();
            statusFilter.getSelectionModel().selectFirst();
            render();
        });

        card.getChildren().addAll(info, sp, manage);
        return card;
    }

    // ---------- Single product's reviews view ----------

    private void renderReviewsForProduct(int productId) {
        reviewList.getChildren().clear();

        Product p = productDao.findById(productId);

        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        Button back = new Button("< Back to products");
        back.getStyleClass().add("btn-ghost");
        back.setOnAction(e -> {
            selectedProductId = null;
            searchField.clear();
            statusFilter.getSelectionModel().selectFirst();
            render();
        });
        Label title = new Label(p == null ? "Reviews" : "Reviews for " + p.getName());
        title.getStyleClass().add("section-title");
        bar.getChildren().addAll(back, title);
        reviewList.getChildren().add(bar);

        String status = statusFilter.getValue();
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        int shown = 0;
        for (Review r : reviewDao.findAll()) {
            if (r.getProductId() != productId) continue;
            if (status != null && !"All".equals(status) && !status.equals(r.getStatus())) continue;
            if (!q.isEmpty()) {
                String hay = (r.getCommentText() + " " + (r.getCustomerName() == null ? "" : r.getCustomerName()))
                        .toLowerCase();
                if (!hay.contains(q)) continue;
            }
            reviewList.getChildren().add(buildCard(r));
            shown++;
        }
        if (shown == 0) {
            Label empty = new Label("No reviews match the current filter for this product.");
            empty.getStyleClass().add("empty-state");
            reviewList.getChildren().add(empty);
        }
    }

    private VBox buildCard(Review r) {
        Product p = productDao.findById(r.getProductId());

        VBox card = new VBox(8);
        card.getStyleClass().add("review-card");

        HBox head = new HBox(10);
        head.setAlignment(Pos.CENTER_LEFT);
        Label who = new Label("#" + r.getId() + "  " +
                (r.getCustomerName() == null ? "Customer" : r.getCustomerName()) +
                "  on  " + (p == null ? "?" : p.getName()));
        who.getStyleClass().add("review-author");
        Label stars = new Label(HomeController.starString(r.getRating()));
        stars.getStyleClass().add("stars");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label status = new Label(r.getStatus());
        status.getStyleClass().add("status-" + r.getStatus().toLowerCase());
        head.getChildren().addAll(who, stars, sp, status);

        Label comment = new Label(r.getCommentText());
        comment.getStyleClass().add("review-comment");
        comment.setWrapText(true);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        Button flag = new Button("Flag");
        flag.getStyleClass().add("btn-ghost");
        Button edit = new Button("Edit");
        edit.getStyleClass().add("btn-ghost");
        Button remove = new Button("Remove");
        remove.getStyleClass().add("btn-danger");
        Button restore = new Button("Restore");
        restore.getStyleClass().add("btn-ghost");

        flag.setOnAction(e -> moderate(r, "FLAG", Review.FLAGGED, "Flagged for review"));
        remove.setOnAction(e -> moderate(r, "DELETE", Review.REMOVED, "Removed by admin"));
        restore.setOnAction(e -> moderate(r, "RESTORE", Review.ACTIVE, "Restored by admin"));
        edit.setOnAction(e -> editComment(r));

        flag.setDisable(Review.FLAGGED.equals(r.getStatus()));
        restore.setDisable(Review.ACTIVE.equals(r.getStatus()));
        remove.setDisable(Review.REMOVED.equals(r.getStatus()));

        actions.getChildren().addAll(flag, edit, remove, restore);
        card.getChildren().addAll(head, comment, actions);
        return card;
    }

    private void moderate(Review r, String action, String newStatus, String note) {
        reviewDao.updateStatus(r.getId(), newStatus);
        logDao.insert(new ModerationLog(Session.getCurrentUser().getId(), r.getId(), action, note));
        productDao.recalculateAverage(r.getProductId());   // status change affects average (FR9)
        Toast.show(root, "Action '" + action + "' applied to review #" + r.getId() + ".", false);
        render();
        renderLog();
    }

    private void editComment(Review r) {
        TextInputDialog dialog = new TextInputDialog(r.getCommentText());
        dialog.setTitle("Edit review comment");
        dialog.setHeaderText("Review #" + r.getId());
        dialog.setContentText("Comment:");
        Optional<String> res = dialog.showAndWait();
        if (res.isPresent()) {
            String text = res.get();
            if (Validation.isBlank(text)) {
                Toast.show(root, "Comment cannot be empty.", true);
                return;
            }
            String bad = Validation.firstDisallowedChar(text);
            if (bad != null) {
                Toast.show(root, "Disallowed character in comment: " + bad, true);
                return;
            }
            reviewDao.updateContent(r.getId(), r.getRating(), text.trim());
            logDao.insert(new ModerationLog(Session.getCurrentUser().getId(), r.getId(), "EDIT", "Comment edited by admin"));
            productDao.recalculateAverage(r.getProductId());
            Toast.show(root, "Review #" + r.getId() + " edited.", false);
            render();
            renderLog();
        }
    }

    private void renderLog() {
        logList.getChildren().clear();
        List<ModerationLog> logs = logDao.findAll();
        if (logs.isEmpty()) {
            Label none = new Label("No moderation actions logged yet.");
            none.getStyleClass().add("hint");
            logList.getChildren().add(none);
            return;
        }
        for (ModerationLog m : logs) {
            Label line = new Label(String.format("%s  ·  %s  ·  review #%d  ·  %s%s",
                    FMT.format(Instant.ofEpochMilli(m.getCreatedAt())),
                    m.getAction(),
                    m.getReviewId(),
                    m.getAdminName() == null ? "admin" : m.getAdminName(),
                    m.getNotes() == null ? "" : "  ·  " + m.getNotes()));
            line.getStyleClass().add("log-line");
            logList.getChildren().add(line);
        }
    }

    @FXML private void applySearch() { render(); }
    @FXML private void clearSearch() {
        searchField.clear();
        statusFilter.getSelectionModel().selectFirst();
        render();
    }
    @FXML private void goHome() { SceneManager.switchTo("home"); }
    @FXML private void logout() { Session.logout(); SceneManager.switchTo("login"); }
}
