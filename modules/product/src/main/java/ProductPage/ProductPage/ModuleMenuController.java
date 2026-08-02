package ProductPage.ProductPage;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ModuleMenuController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label firstTitle;
    @FXML private Label firstCaption;
    @FXML private Label secondTitle;
    @FXML private Label secondCaption;
    @FXML private Label statusLabel;
    @FXML private Button secondButton;

    private String module;
    private String firstView;
    private String secondView;

    public void configure(String title, String subtitle, String module,
            String firstTitleText, String firstCaptionText, String firstView,
            String secondTitleText, String secondCaptionText, String secondView) {
        titleLabel.setText(title);
        subtitleLabel.setText(subtitle);
        this.module = module;
        this.firstView = firstView;
        this.secondView = secondView;
        firstTitle.setText(firstTitleText);
        firstCaption.setText(firstCaptionText);
        secondTitle.setText(secondTitleText);
        secondCaption.setText(secondCaptionText);
        secondButton.setVisible(secondView != null);
        secondButton.setManaged(secondView != null);
    }

    @FXML
    private void openFirst() {
        launch(firstView);
    }

    @FXML
    private void openSecond() {
        launch(secondView);
    }

    private void launch(String view) {
        try {
            ModuleLauncher.launch(module, view);
            statusLabel.setText("Opening " + module + "...");
        } catch (IOException ex) {
            ex.printStackTrace();
            statusLabel.setText("Could not launch " + module + ": " + ex.getMessage());
        }
    }

    @FXML
    private void backToHub(ActionEvent event) throws IOException {
        Parent root = App.loadFXML("Team Hub");
        Scene scene = ((Node) event.getSource()).getScene();
        scene.setRoot(root);
    }
}
