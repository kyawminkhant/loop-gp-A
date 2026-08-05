package ProductPage.ProductPage;

import java.io.IOException;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class ProductMenuController {

    public Label statusLabel;

    public void initialize() {
        statusLabel.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> newScene.getRoot()
                        .lookupAll(".product-menu-card")
                        .forEach(this::animateMenuCard));
            }
        });
    }

    public void openMainPage() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("Main Page.fxml"));
            Parent root = loader.load();

            App.setRoot(root);
        } catch (IOException ex) {
            ex.printStackTrace();
            statusLabel.setText("Could not open Main Page.fxml. Check the file name and controller.");
        }
    }

    public void openSellerPage() {
        ProductManagementContext.clearReturnToAdmin();
        setRoot("Product Manager");
    }

    private void setRoot(String fxmlName) {
        try {
            App.setRoot(fxmlName);
        } catch (IOException ex) {
            ex.printStackTrace();
            statusLabel.setText("Could not open " + fxmlName + ".fxml.");
        }
    }

    private void animateMenuCard(Node card) {
        card.setOpacity(0);
        card.setTranslateY(22);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(380), card);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(380), card);
        slideIn.setFromY(22);
        slideIn.setToY(0);

        fadeIn.play();
        slideIn.play();

        card.setOnMouseEntered(event -> scale(card, 1.045));
        card.setOnMouseExited(event -> scale(card, 1.0));
    }

    private void scale(Node node, double value) {
        Object oldAnimation = node.getProperties().get("hubScaleAnimation");
        if (oldAnimation instanceof ScaleTransition) {
            ((ScaleTransition) oldAnimation).stop();
        }

        ScaleTransition scale = new ScaleTransition(Duration.millis(140), node);
        scale.setToX(value);
        scale.setToY(value);
        scale.setOnFinished(event -> {
            node.setScaleX(value);
            node.setScaleY(value);
            node.getProperties().remove("hubScaleAnimation");
        });
        node.getProperties().put("hubScaleAnimation", scale);
        scale.play();
    }
}
