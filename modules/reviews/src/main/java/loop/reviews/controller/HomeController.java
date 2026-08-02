package loop.reviews.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import loop.reviews.SceneManager;
import loop.reviews.Session;
import loop.reviews.db.OrderDao;
import loop.reviews.db.ProductDao;
import loop.reviews.model.Product;
import loop.reviews.model.User;

import java.util.List;

/** FR2 - browse products; entry point to reviews for each product. */
public class HomeController {

    @FXML private Label welcomeLabel;
    @FXML private Button myReviewsButton;
    @FXML private Button adminButton;
    @FXML private VBox productList;

    private final ProductDao productDao = new ProductDao();
    private final OrderDao orderDao = new OrderDao();

    @FXML
    private void initialize() {
        User u = Session.getCurrentUser();
        welcomeLabel.setText(u == null ? "" : "Signed in as " + u.getName());

        boolean admin = Session.isAdmin();
        // Admins moderate; customers browse and manage their own reviews.
        myReviewsButton.setVisible(!admin);
        myReviewsButton.setManaged(!admin);
        adminButton.setVisible(admin);
        adminButton.setManaged(admin);

        loadProducts();
    }

    private void loadProducts() {
        productList.getChildren().clear();
        List<Integer> purchased = (Session.getCurrentUser() == null)
            ? List.of()
            : orderDao.findPurchasedProductIds(Session.getCurrentUser().getId());

        for (Product p : productDao.findAll()) {
            productList.getChildren().add(buildCard(p, purchased.contains(p.getId())));
        }
    }

    private HBox buildCard(Product p, boolean purchased) {
        HBox card = new HBox(16);
        card.getStyleClass().add("product-card");
        card.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(4);
        Label name = new Label(p.getName());
        name.getStyleClass().add("product-name");
        Label meta = new Label(String.format("%s  ·  £%.2f  ·  stock: %d",
                p.getCategory() == null ? "" : p.getCategory(), p.getPrice(), p.getStock()));
        meta.getStyleClass().add("product-meta");
        info.getChildren().addAll(name, meta);

        Label stars = new Label(starString(p.getAverageRating())
                + String.format("  %.1f", p.getAverageRating()));
        stars.getStyleClass().add("stars");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label(purchased ? "Purchased" : "Not purchased");
        badge.getStyleClass().add(purchased ? "badge-purchased" : "badge-muted");

        Button view = new Button("View Reviews");
        view.getStyleClass().add("btn-primary");
        view.setOnAction(e -> {
            Session.setSelectedProductId(p.getId());
            SceneManager.switchTo("product_reviews");
        });

        card.getChildren().addAll(info, spacer, stars, badge, view);
        return card;
    }

    /** Rounded star glyph string for an average rating. */
    static String starString(double avg) {
        int full = (int) Math.round(avg);
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) sb.append(i <= full ? '★' : '☆');
        return sb.toString();
    }

    @FXML private void openMyReviews() { SceneManager.switchTo("my_reviews"); }
    @FXML private void openAdmin() { SceneManager.switchTo("admin_moderation"); }
}
